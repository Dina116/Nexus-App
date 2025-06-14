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

                        val activeParticipantId = participantManager.getCurrentParticipantId()

                        val participantId = activeParticipantId ?: "local-user"

                        Log.d("CameraManager", "Active participant ID: $participantId")

                        DetectionResultManager.addResult(
                            DetectionResult(
                                result = result,
                                photoPath = photoFile.absolutePath,
                                timestamp = System.currentTimeMillis(),
                                activeParticipantId = participantId
                            )
                        )

                        val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
                        val currentMeetingId = sharedPrefs.getString("currentMeetingId", "") ?: ""

                        if (currentMeetingId.isNotEmpty()) {
                            FirebaseManager.addDetectionResult(currentMeetingId, participantId, result!!) { success ->
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
                else {
                    Log.d("CameraManager", "Firebase results found: ${firebaseResults.size}")
                    createPdfFromFirebaseResults(meetingName, pdfFile, firebaseResults)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to create final PDF: ${e.message}")
            Toast.makeText(context, "Failed to create report: ${e.message}", Toast.LENGTH_LONG).show()
        }}


    private fun createPdfFromLocalResults(meetingName: String, pdfFile: File) {
        val detectionResults = DetectionResultManager.getResults()
        Log.d("CameraManager", "Creating final report with ${detectionResults.size} local results")
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()
        document.add(Paragraph("Meeting Report : $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
        document.add(Paragraph("Date & Time: ${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())}"))
        document.add(Paragraph("Total Detections: ${detectionResults.size}"))
        document.add(Paragraph("Total Participants: ${participantManager.getParticipantCount()}"))
        document.add(Paragraph("\n"))
        if (detectionResults.isEmpty()) {
            document.add(Paragraph("No detection results available."))
        } else {
            val participants = participantManager.getParticipants()
            createParticipantSummaryTable(document, detectionResults, participants)
        }

        document.close()
        DetectionResultManager.clearResults()
        participantManager.clearParticipants()
        Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")
        openPdfReport(pdfFile)
    }

     private fun createPdfFromFirebaseResults(meetingName: String, pdfFile: File, firebaseResults: List<DetectionResultData>) {
        Log.d("CameraManager", "Creating final report with ${firebaseResults.size} Firebase results")
        FirebaseManager.getMeetingParticipants(meetingName) { participants ->
            val participantsMap = participants.associate { it.participantId to it.displayName }
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
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()
            document.add(Paragraph("Meeting Report (All Participants): $meetingName", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)))
            document.add(Paragraph("Date & Time: ${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())}"))
            document.add(Paragraph("Total Detections: ${detectionResults.size}"))
            document.add(Paragraph("Total Participants: ${participantsMap.size}"))
            document.add(Paragraph("\n"))
            if (detectionResults.isEmpty()) {
                document.add(Paragraph("No detection results available."))
            } else {
                createParticipantSummaryTable(document, detectionResults, participantsMap)
            }
            document.close()
            Log.d("CameraManager", "Final PDF report saved: ${pdfFile.absolutePath}")
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

        if(isCheatingDetectionEnabled ){
            addTableCell(table, "Participant", true)
            addTableCell(table, "Cheating%", true)
            addTableCell(table, "Mobile Usage ", true)
            addTableCell(table, "No Attendance (min)", true)
            addTableCell(table, "People Count", true)
            addTableCell(table, "People Time (min)", true)
            addTableCell(table, "Sleep Time (min)", true)
            addTableCell(table, "Total Detections", true)
        }
        else{
            addTableCell(table, "Participant", true)
            addTableCell(table, "Attention %", true)
            addTableCell(table, "Mobile Usage ", true)
            addTableCell(table, "No Attendance (min)", true)
            addTableCell(table, "People Count", true)
            addTableCell(table, "People Time (min)", true)
            addTableCell(table, "Sleep Time (min)", true)
            addTableCell(table, "Total Detections", true)
        }

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
        var totalMobileDetections = 0.0
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
                    totalPeopleCount += result.People_count
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
                    totalPeopleCount += result.People_count
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
            val mobileUsed = totalMobileDetections > 0
            addTableCell(table, mobileUsed.toString())
//            val mobilePercentage = (totalMobileDetections.toDouble() / resultCount) * 100
//            addTableCell(table, "${String.format("%.2f", mobilePercentage)}%")
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgNoAttendanceTime = (totalNoAttendanceTime / resultCount)/1000/ 60
//            addTableCell(table, String.format("%d", avgNoAttendanceTime))
            addTableCell(table, avgNoAttendanceTime.toInt().toString())
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
            val avgPeopleTime = (totalPeopleTime / resultCount) / 1000 / 60
//            addTableCell(table, String.format("%d", avgPeopleTime))
            addTableCell(table, avgPeopleTime.toInt().toString())
        } else {
            addTableCell(table, "N/A")
        }
        if (resultCount > 0) {
            val avgSleepTime = (totalSleepTime / resultCount)/1000/60
//            addTableCell(table, String.format("%d", avgSleepTime))
            addTableCell(table, avgSleepTime.toInt().toString())
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

        val chooserIntent = Intent.createChooser(intent, "Open PDF with...")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Log.e("PDFViewer", "Error opening PDF: ${e.message}")
        Log.d("PDFReport", "File saved at: ${file.absolutePath}")
        Toast.makeText(context, "Cannot open PDF. Please install a PDF viewer app.", Toast.LENGTH_LONG).show()
    }
}

    fun release() {
        stopImageCaptureLoop()
        cameraProvider?.unbindAll()
    }
}
