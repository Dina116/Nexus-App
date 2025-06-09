package com.training.graduation.screens.startmeeting

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import com.itextpdf.text.BaseColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.util.Date

class CameraManager private constructor (private val context: Context) {
    companion object {
        @Volatile
        private var instance: CameraManager? = null

        fun getInstance(context: Context): CameraManager {
            return instance ?: synchronized(this) {
                instance ?: CameraManager(context.applicationContext).also { instance = it }
            }
        }
    }
    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File
    private var cameraProvider: ProcessCameraProvider? = null
    private var isCapturing = false
    private val captureInterval = 10000L
    private var isCheatingDetectionEnabled = false


    data class DetectionResult(
        val result: Any?,
        val photoPath: String,
        val timestamp: Long,
        val activeParticipantId: String?
    )

    init {
        initializeOutputDirectory()
    }
    private val participantManager = ParticipantManager.getInstance()

    fun initializeOutputDirectory() {
        outputDirectory = File(context.filesDir, "photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, onInitialized: () -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, cameraSelector, imageCapture
                )
                onInitialized()
            } catch (exc: Exception) {
                Log.e("CameraManager", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setCheatingDetectionEnabled(enabled: Boolean) {
        isCheatingDetectionEnabled = enabled
    }

    fun startImageCaptureLoop() {
        isCapturing = true
        captureLoop()
    }

    private fun captureLoop() {
        if (!isCapturing) return

        takePhoto()

        Handler(Looper.getMainLooper()).postDelayed({
            captureLoop()
        }, captureInterval)
    }

    fun stopImageCaptureLoop() {
        isCapturing = false
    }

    private fun takePhoto() {
        if (!::imageCapture.isInitialized) {
            Log.e("CameraManager", "Camera is not initialized yet.")
            return
        }

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraManager", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraManager", "Photo capture succeeded: ${photoFile.absolutePath}")

                    //ParticipantManager.getInstance().setCurrentParticipantId(participantId)
                    sendPhotoToServer(photoFile, isCheatingDetectionEnabled)
                }
            }
        )
    }

    private fun sendPhotoToServer(photoFile: File, isCheating: Boolean) {
        val api = RetrofitInstance.api
        val imagePart = prepareImagePart(photoFile)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (imagePart != null) {
                    val response = if (isCheating) {
                        api.detectCheating(imagePart)
                    } else {
                        api.detectFocus(imagePart)
                    }

                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d("CameraManager", "Result: $result")
                        Log.d("CameraManager", "Adding result to detectionResults")

                        //val activeParticipantId = participantManager.getParticipants().keys.firstOrNull()

                        val activeParticipantId = participantManager.getCurrentParticipantId()

                        Log.d("CameraManager", "Active participant ID: $activeParticipantId")
                        Log.d("CameraManager", "Participant count: ${participantManager.getParticipantCount()}")

                            DetectionResultManager.addResult(
                                DetectionResult(
                                    result = result,
                                    photoPath = photoFile.absolutePath,
                                    timestamp = System.currentTimeMillis(),
                                    activeParticipantId = activeParticipantId
                                )
                            )
                        val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
                        val currentMeetingId = sharedPrefs.getString("currentMeetingId", "") ?: ""

                        if (currentMeetingId.isNotEmpty()) {
                            FirebaseManager.addDetectionResult(currentMeetingId, activeParticipantId.toString(), result!!) { success ->
                                if (success) {
                                    Log.d("CameraManager", "Result uploaded to Firebase")
                                } else {
                                    Log.e("CameraManager", "Failed to upload result to Firebase")
                                }
                            }
                        }
                    } else {
                        Log.e("CameraManager", "Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraManager", "Exception: ${e.message}")
            }
        }
    }


    private fun prepareImagePart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, requestFile)
    }

    fun createFinalReport(meetingName: String) {
        try {
            Log.d("CameraManager", "Starting to create final report for meeting: $meetingName")
            val detectionResults = DetectionResultManager.getResults()
            Log.d("CameraManager", "Creating final report with ${detectionResults.size} results from DetectionResultManager")

            val pdfDir = File(context.filesDir, "pdf_reports").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())
            val pdfFile = File(pdfDir, "meeting_report_${meetingName}_${timestamp}.pdf")

            FirebaseManager.getMeetingResults(meetingName) { firebaseResults ->
                if (firebaseResults == null || firebaseResults.isEmpty()) {
                    Log.d("CameraManager", "No Firebase results found, using local results.")
                    createPdfFromLocalResults(meetingName, pdfFile)
                }
//                if (firebaseResults.isEmpty()) {
//                    // إذا لم تكن هناك نتائج في Firebase، استخدم النتائج المحلية
//                    createPdfFromLocalResults(meetingName, pdfFile) }
                else {
                    Log.d("CameraManager", "Firebase results found: ${firebaseResults.size}")
                    createPdfFromFirebaseResults(meetingName, pdfFile, firebaseResults)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to create final PDF: ${e.message}")
            Toast.makeText(context, "Failed to create report: ${e.message}", Toast.LENGTH_LONG).show()
        }}

//
//            val document = Document()
//            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
//            document.open()
//            document.add(Paragraph("Meeting Summary Report: $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
//            document.add(Paragraph("Date & Time: $timestamp"))
//            document.add(Paragraph("Total Detections: ${detectionResults.size}"))
//            document.add(Paragraph("\n"))
//            if (detectionResults.isEmpty()) {
//                document.add(Paragraph("No detection results available."))
//            } else {
//                val participants = participantManager.getParticipants()
//                createParticipantSummaryTable(document, detectionResults, participants)
//            }
//            document.close()
//            DetectionResultManager.clearResults()
//            participantManager.clearParticipants()
//            Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")
//            Log.d("CameraManager", "PDF directory path: ${pdfDir.absolutePath}")
//            Log.d("CameraManager", "PDF directory exists: ${pdfDir.exists()}")
//            openPdfReport(pdfFile)
//
//        } catch (e: Exception) {
//            Log.e("CameraManager", "Failed to create final PDF: ${e.message}")
//        }
//    }

  /*  fun createPdfFromLocalResults(meetingName: String, pdfFile: File) {
        try {
            Log.d("CameraManager", "Creating PDF from local results at: ${pdfFile.absolutePath}")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
            val title = Paragraph("Meeting Report - $meetingName", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))

            val results = DetectionResultManager.getResults()
            val participantsMap = ParticipantManager.getInstance().getParticipants()

            if (results.isEmpty() && participantsMap.isEmpty()) {
                document.add(Paragraph("No data available for this meeting."))
            } else {
                // بيانات المشاركين
                val participantTitle = Paragraph("Participants", Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD))
                participantTitle.alignment = Element.ALIGN_LEFT
                document.add(participantTitle)
                document.add(Paragraph("\n"))

                participantsMap.forEach { (participantId, displayName) ->
                    document.add(Paragraph("Name: $displayName"))
                    document.add(Paragraph("Participant ID: $participantId"))
                    document.add(Paragraph("\n"))
                }

                // نتائج الكشف
                val detectionTitle = Paragraph("Detection Results", Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD))
                detectionTitle.alignment = Element.ALIGN_LEFT
                document.add(detectionTitle)
                document.add(Paragraph("\n"))

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

                results.forEach { result ->
                    val faceName = when (val r = result.result) {
                        is String -> r
                        else -> "Unknown Face"
                    }
                    val timeStr = sdf.format(Date(result.timestamp))
                    val participantName = result.activeParticipantId?.let {
                        ParticipantManager.getInstance().getParticipantName(it)
                    } ?: "Unknown Participant"

                    document.add(Paragraph("Face: $faceName"))
                    document.add(Paragraph("Detected At: $timeStr"))
                    document.add(Paragraph("Associated Participant: $participantName"))
                    document.add(Paragraph("Photo Path: ${result.photoPath}"))
                    document.add(Paragraph("\n"))
                }
            }

            document.close()
            openPdfReport(pdfFile)

        } catch (e: Exception) {
            Log.e("CameraManager", "Error creating PDF from local results: ${e.message}")
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }*/

    private fun createPdfFromLocalResults(meetingName: String, pdfFile: File) {
        val detectionResults = DetectionResultManager.getResults()
        Log.d("CameraManager", "Creating final report with ${detectionResults.size} local results")

        // استخدم الكود الحالي لإنشاء PDF من النتائج المحلية
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()

        // إضافة عنوان
        document.add(Paragraph("Meeting Report (Local): $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
        document.add(Paragraph("Date & Time: ${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())}"))
        document.add(Paragraph("Total Detections: ${detectionResults.size}"))
        document.add(Paragraph("Total Participants: ${participantManager.getParticipantCount()}"))
        document.add(Paragraph("\n"))

        // استخدم الكود الحالي لإضافة النتائج إلى PDF
        if (detectionResults.isEmpty()) {
            document.add(Paragraph("No detection results available."))
        } else {
            val participants = participantManager.getParticipants()
            createParticipantSummaryTable(document, detectionResults, participants)
        }

        document.close()

        // مسح النتائج بعد إنشاء التقرير
        DetectionResultManager.clearResults()
        participantManager.clearParticipants()

        Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")

        // فتح التقرير
        openPdfReport(pdfFile)
    }

     private fun createPdfFromFirebaseResults(meetingName: String, pdfFile: File, firebaseResults: List<DetectionResultData>) {
        Log.d("CameraManager", "Creating final report with ${firebaseResults.size} Firebase results")

        // الحصول على المشاركين من Firebase
        FirebaseManager.getMeetingParticipants(meetingName) { participants ->
            val participantsMap = participants.associate { it.participantId to it.displayName }

            // تحويل نتائج Firebase إلى نتائج محلية
            val detectionResults = firebaseResults.map { firebaseResult ->
                val result = if (firebaseResult.resultType == "cheating") {
                    CheatingResult(
                        Mobile = firebaseResult.isMobile,
                        No_attendance_time = firebaseResult.noAttendanceTime,
                        People_count = firebaseResult.peopleCount,
                        People_time = firebaseResult.peopleTime,
                        Percentage_of_cheating = firebaseResult.percentage,
                        Sleep_time = firebaseResult.sleepTime
                    )
                } else {
                    AttentionResult(
                        Mobile = firebaseResult.isMobile,
                        No_attendance_time = firebaseResult.noAttendanceTime,
                        People_count = firebaseResult.peopleCount,
                        People_time = firebaseResult.peopleTime,
                        Percentage_of_attention = firebaseResult.percentage,
                        Sleep_time = firebaseResult.sleepTime
                    )
                }

                DetectionResult(
                    result = result,
                    photoPath = "",
                    timestamp = firebaseResult.timestamp,
                    activeParticipantId = firebaseResult.participantId
                )
            }

            // إنشاء PDF
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // إضافة عنوان
            document.add(Paragraph("Meeting Report (All Participants): $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
            document.add(Paragraph("Date & Time: ${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())}"))
            document.add(Paragraph("Total Detections: ${detectionResults.size}"))
            document.add(Paragraph("Total Participants: ${participantsMap.size}"))
            document.add(Paragraph("\n"))

            // إضافة النتائج إلى PDF
            if (detectionResults.isEmpty()) {
                document.add(Paragraph("No detection results available."))
            } else {
                createParticipantSummaryTable(document, detectionResults, participantsMap)
            }

            document.close()

            Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")

            // فتح التقرير
            openPdfReport(pdfFile)
        }
    }

    private fun createParticipantSummaryTable(document: Document, allResults: List<DetectionResult>, participants: Map<String, String>) {
        document.add(Paragraph("Participants Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f)))
        val table = PdfPTable(8)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.spacingAfter = 10f
        val columnWidths = floatArrayOf(3f, 2f, 2f, 2f, 2f, 2f, 2f, 2f)
        table.setWidths(columnWidths)

        addTableCell(table, "Participant", true)
        addTableCell(table, "Cheating/Attention %", true)
        addTableCell(table, "Mobile Usage %", true)
        addTableCell(table, "No Attendance (sec)", true)
        addTableCell(table, "People Count", true)
        addTableCell(table, "People Time (sec)", true)
        addTableCell(table, "Sleep Time (sec)", true)
        addTableCell(table, "Total Detections", true)
        if (participants.isEmpty()) {
            addParticipantRow(table, "All Participants", allResults)
        } else {
            for ((participantId, participantName) in participants) {
                val resultsForParticipant = allResults.filter { it.activeParticipantId == participantId }
                if (resultsForParticipant.isNotEmpty()) {
                    addParticipantRow(table, participantName, resultsForParticipant)
                }
            }
            val unassignedResults = allResults.filter { it.activeParticipantId == null }
            if (unassignedResults.isNotEmpty()) {
                addParticipantRow(table, "Unassigned", unassignedResults)
            }
            addParticipantRow(table, "Overall Average", allResults)
        }
        document.add(table)
    }

    private fun addParticipantRow(table: PdfPTable, participantName: String, results: List<DetectionResult>) {
        var totalCheatingPercentage = 0.0
        var totalAttentionPercentage = 0.0
        var cheatingCount = 0
        var attentionCount = 0
        var totalMobileDetections = 0
        var totalNoAttendanceTime = 0.0
        var totalPeopleCount = 0.toInt()
        var totalPeopleTime = 0.0
        var totalSleepTime = 0.0
        var resultCount = results.size
        results.forEach { detectionResult ->
            when (val result = detectionResult.result) {
                is CheatingResult -> {
                    try {
                        val percentage = result.Percentage_of_cheating.replace("%", "").toDouble()
                        totalCheatingPercentage += percentage
                        cheatingCount++
                    } catch (e: Exception) {
                        Log.e("CameraManager", "Error parsing cheating percentage: ${e.message}")
                    }
                    if (result.Mobile) totalMobileDetections++
                    totalNoAttendanceTime += result.No_attendance_time
                    totalPeopleCount += result.People_count.toInt()
                    totalPeopleTime += result.People_time
                    totalSleepTime += result.Sleep_time
                }
                is AttentionResult -> {
                    try {
                        val percentage = result.Percentage_of_attention.replace("%", "").toDouble()
                        totalAttentionPercentage += percentage
                        attentionCount++
                    } catch (e: Exception) {
                        Log.e("CameraManager", "Error parsing attention percentage: ${e.message}")
                    }
                    if (result.Mobile) totalMobileDetections++
                    totalNoAttendanceTime += result.No_attendance_time
                    totalPeopleCount += result.People_count.toInt()
                    totalPeopleTime += result.People_time
                    totalSleepTime += result.Sleep_time
                }
            }
        }
        addTableCell(table, participantName)
        if (cheatingCount > 0) {
            val avgCheating = totalCheatingPercentage / cheatingCount
            addTableCell(table, "${String.format("%.2f", avgCheating)}%")
        } else if (attentionCount > 0) {
            val avgAttention = totalAttentionPercentage / attentionCount
            addTableCell(table, "${String.format("%.2f", avgAttention)}%")
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val mobilePercentage = (totalMobileDetections.toDouble() / resultCount) * 100
            addTableCell(table, "${String.format("%.2f", mobilePercentage)}%")
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgNoAttendanceTime = totalNoAttendanceTime / resultCount
            addTableCell(table, String.format("%.2f", avgNoAttendanceTime))
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgPeopleCount = totalPeopleCount.toInt() / resultCount
            addTableCell(table, String.format("%d", avgPeopleCount))
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgPeopleTime = totalPeopleTime / resultCount
            addTableCell(table, String.format("%.2f", avgPeopleTime))
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgSleepTime = totalSleepTime / resultCount
            addTableCell(table, String.format("%.2f", avgSleepTime))
        } else {
            addTableCell(table, "N/A")
        }
        addTableCell(table, resultCount.toString())
    }
    private fun addTableCell(table: PdfPTable, text: String, isHeader: Boolean = false) {
        val cell = PdfPCell(Phrase(text))
        if (isHeader) {
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.horizontalAlignment = Element.ALIGN_CENTER
        }
        cell.paddingTop = 5f
        cell.paddingBottom = 5f
        table.addCell(cell)
    }

    fun getPdfReportsList(): List<File> {
        val pdfDir = File(context.filesDir, "pdf_reports")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
            return emptyList()
        }

        return pdfDir.listFiles { file -> file.name.endsWith(".pdf") }?.toList() ?: emptyList()
    }
    fun openPdfReport(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PDFViewer", "Error opening PDF: ${e.message}")
            Toast.makeText(context, "Cannot open PDF. Please install a PDF viewer app.", Toast.LENGTH_LONG).show()
        }
    }
    fun release() {
        stopImageCaptureLoop()
        cameraProvider?.unbindAll()
    }
}

/*
//    fun createFinalReport(meetingName: String) {
//        try {
//            Log.d("CameraManager", "Starting to create final report for meeting: $meetingName")
//
//            // الحصول على النتائج من DetectionResultManager
//            val detectionResults = DetectionResultManager.getResults()
//
//            Log.d("CameraManager", "Creating final report with ${detectionResults.size} results from DetectionResultManager")
//
//            val pdfDir = File(context.filesDir, "pdf_reports").apply {
//                if (!exists()) {
//                    mkdirs()
//                }
//            }
//
//
//            // إنشاء اسم الملف بناءً على اسم الميتنج والتاريخ
//            val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())
//            val pdfFile = File(pdfDir, "meeting_report_${meetingName}_${timestamp}.pdf")
//
//            // إنشاء الـ PDF باستخدام مكتبة iText
//            val document = Document()
//            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
//            document.open()
//
//            // إضافة عنوان
//            document.add(Paragraph("Meeting Report: $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
//            document.add(Paragraph("Date & Time: $timestamp"))
//            document.add(Paragraph("Total Detections: ${detectionResults.size}"))
//            document.add(Paragraph("Total Participants: ${participantManager.getParticipantCount()}"))
//            document.add(Paragraph("\n"))
//
//            // تجميع النتائج حسب المشاركين
//
//                if (detectionResults.isEmpty()) {
//                    document.add(Paragraph("No detection results available."))
//                } else {
//                    // تحديد المشاركين المختلفين
//                    val participants = participantManager.getParticipants()
//
//                    if (participants.isEmpty()) {
//                        // إذا لم يتم تسجيل أي مشاركين، نستخدم تقسيم بديل
//                        document.add(Paragraph("No participants detected. Grouping results by time periods.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
//
//                        // تقسيم النتائج حسب فترات زمنية (كل 15 دقيقة)
//                        val timeGroups = detectionResults.groupBy {
//                            it.timestamp / (15 * 60 * 1000) // تقسيم حسب فترات 15 دقيقة
//                        }
//
//                        for ((timeGroup, resultsInGroup) in timeGroups) {
//                            val startTime = SimpleDateFormat("HH:mm", Locale.US).format(timeGroup * 15 * 60 * 1000)
//                            val endTime = SimpleDateFormat("HH:mm", Locale.US).format((timeGroup + 1) * 15 * 60 * 1000)
//
//                            document.add(Paragraph("Time Period: $startTime - $endTime", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f)))
//                            document.add(Paragraph("{"))
//
//                            // إضافة النتائج لهذه الفترة الزمنية
//                            addResultsToDocument(document, resultsInGroup)
//
//                            document.add(Paragraph("}"))
//                            document.add(Paragraph("\n"))
//                        }
//                    } else {
//                        // تجميع النتائج حسب المشاركين
//                        for ((participantId, participantName) in participants) {
//                            // تجميع النتائج لهذا المشارك
//                            val resultsForThisParticipant = detectionResults.filter { it.activeParticipantId == participantId }
//
//                            if (resultsForThisParticipant.isNotEmpty()) {
//                                document.add(Paragraph("Participant: $participantName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f)))
//                                document.add(Paragraph("{"))
//
//                                // إضافة النتائج لهذا المشارك
//                                addResultsToDocument(document, resultsForThisParticipant)
//
//                                document.add(Paragraph("}"))
//                                document.add(Paragraph("\n"))
//                            }
//                        }
//
//                        // إضافة النتائج التي لا تنتمي لأي مشارك
//                        val unassignedResults = detectionResults.filter { it.activeParticipantId == null }
//                        if (unassignedResults.isNotEmpty()) {
//                            document.add(Paragraph("Unassigned Results", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f)))
//                            document.add(Paragraph("{"))
//
//                            // إضافة النتائج غير المرتبطة بمشارك
//                            addResultsToDocument(document, unassignedResults)
//
//                            document.add(Paragraph("}"))
//                            document.add(Paragraph("\n"))
//                        }else{}
//                    }
//                }
//
//
//            document.close()
//
//            // مسح القائمة بعد إنشاء التقرير
////            synchronized(resultsLock) {
////                detectionResults.clear()
////            }
//
//            // مسح قائمة المشاركين
//            DetectionResultManager.clearResults()
//            participantManager.clearParticipants()
//
//            Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")
//            Log.d("CameraManager", "PDF directory path: ${pdfDir.absolutePath}")
//            Log.d("CameraManager", "PDF directory exists: ${pdfDir.exists()}")
//            openPdfReport(pdfFile)
//
//
//
//        } catch (e: Exception) {
//            Log.e("CameraManager", "Failed to create final PDF: ${e.message}")
//        }
//    }
//    private fun addResultsToDocument(document: Document, results: List<DetectionResult>) {
//        var totalCheatingPercentage = 0.0
//        var totalAttentionPercentage = 0.0
//        var cheatingCount = 0
//        var attentionCount = 0
//
//        results.forEachIndexed { index, detectionResult ->
//            document.add(Paragraph("Detection #${index + 1}", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
//            document.add(Paragraph("Time: ${SimpleDateFormat("HH:mm:ss", Locale.US).format(detectionResult.timestamp)}"))
//
//            // إضافة الصورة
//            try {
//                val image = Image.getInstance(detectionResult.photoPath)
//                image.scaleToFit(300f, 300f)
//                document.add(image)
//            } catch (e: Exception) {
//                document.add(Paragraph("Image not available"))
//            }
//
//            // إضافة نتائج الكشف
//            document.add(Paragraph("Results:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
//
//            when (val result = detectionResult.result) {
//                is CheatingResult -> {
//                    document.add(Paragraph("Mobile Detected: ${result.Mobile}"))
//                    document.add(Paragraph("No Attendance Time: ${result.No_attendance_time} seconds"))
//                    document.add(Paragraph("People Count: ${result.People_count}"))
//                    document.add(Paragraph("People Time: ${result.People_time} seconds"))
//                    document.add(Paragraph("Percentage of Cheating: ${result.Percentage_of_cheating}"))
//                    document.add(Paragraph("Sleep Time: ${result.Sleep_time} seconds"))
//
//                    // تحويل النسبة من نص إلى رقم
//                    try {
//                        val percentage = result.Percentage_of_cheating.replace("%", "").toDouble()
//                        totalCheatingPercentage += percentage
//                        cheatingCount++
//                    } catch (e: Exception) {
//                        Log.e("CameraManager", "Error parsing cheating percentage: ${e.message}")
//                    }
//                }
//                is AttentionResult -> {
//                    document.add(Paragraph("Mobile Detected: ${result.Mobile}"))
//                    document.add(Paragraph("No Attendance Time: ${result.No_attendance_time} seconds"))
//                    document.add(Paragraph("People Count: ${result.People_count}"))
//                    document.add(Paragraph("People Time: ${result.People_time} seconds"))
//                    document.add(Paragraph("Percentage of Attention: ${result.Percentage_of_attention}"))
//                    document.add(Paragraph("Sleep Time: ${result.Sleep_time} seconds"))
//
//                    // تحويل النسبة من نص إلى رقم
//                    try {
//                        val percentage = result.Percentage_of_attention.replace("%", "").toDouble()
//                        totalAttentionPercentage += percentage
//                        attentionCount++
//                    } catch (e: Exception) {
//                        Log.e("CameraManager", "Error parsing attention percentage: ${e.message}")
//                    }
//                }
//            }
//
//            document.add(Paragraph("\n"))
//        }
//
//        // إضافة متوسط النسب
//        document.add(Paragraph("Summary:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
//        if (cheatingCount > 0) {
//            val avgCheating = totalCheatingPercentage / cheatingCount
//            document.add(Paragraph("Average Cheating Percentage: ${String.format("%.2f", avgCheating)}%"))
//        }
//        if (attentionCount > 0) {
//            val avgAttention = totalAttentionPercentage / attentionCount
//            document.add(Paragraph("Average Attention Percentage: ${String.format("%.2f", avgAttention)}%"))
//        }
//    }*/


/*    private fun sendPhotoToServer(photoFile: File, isCheating: Boolean) {
        val api = RetrofitInstance.api
        val imagePart = prepareImagePart(photoFile)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // تأكد من تحديث الـ participantId قبل الإرسال
                val participants = participantManager.getParticipants()
                val activeParticipantId = participantManager.getParticipants().keys.firstOrNull()?:"local-user"
                participantManager.setCurrentParticipantId(activeParticipantId)

                if (imagePart != null) {
                    val response = if (isCheating) {
                        api.detectCheating(imagePart)
                    } else {
                        api.detectFocus(imagePart)
                    }

                    if (response.isSuccessful) {
                        val result = response.body()

                        DetectionResultManager.addResult(
                            DetectionResult(
                                result = result,
                                photoPath = photoFile.absolutePath,
                                timestamp = System.currentTimeMillis(),
                                activeParticipantId = activeParticipantId
                            )
                        )

                        // ارفع للفايربيز
                        val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
                        val currentMeetingId = sharedPrefs.getString("currentMeetingId", "") ?: ""

                        if (currentMeetingId.isNotEmpty()) {
                            FirebaseManager.addDetectionResult(currentMeetingId, activeParticipantId, result!!) { success ->
                                if (success) {
                                    Log.d("CameraManager", "Result uploaded to Firebase")
                                } else {
                                    Log.e("CameraManager", "Failed to upload result to Firebase")
                                }
                            }
                        }
                    } else {
                        Log.e("CameraManager", "Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraManager", "Exception: ${e.message}")
            }
        }
    }
*/