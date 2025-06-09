package com.training.graduation.screens.startmeeting

data class DetectionResultData(
    val resultId: String = "", // معرف النتيجة
    val meetingId: String = "", // معرف الميتنج
    val participantId: String = "", // معرف المشارك
    val timestamp: Long = 0, // وقت التحليل
    val resultType: String = "", // نوع النتيجة (غش أو تركيز)
    val resultJson: String = "", // نتيجة التحليل كـ JSON
    val starttime:Long=0,
    val endtime:Long=0,

    val isMobile: Boolean = false, // هل تم اكتشاف موبايل
    val noAttendanceTime: Double = 0.0, // وقت عدم الحضور
    val peopleCount: Int = 0, // عدد الأشخاص
    val peopleTime: Double = 0.0, // وقت وجود الأشخاص
    val percentage: String = "", // نسبة الغش أو التركيز
    val sleepTime: Double = 0.0 // وقت النوم
)

