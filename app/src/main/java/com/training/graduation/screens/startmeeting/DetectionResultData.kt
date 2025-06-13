package com.training.graduation.screens.startmeeting

data class DetectionResultData(
    val resultId: String = "",
    val meetingId: String = "",
    val participantId: String = "",
    val timestamp: Long = 0,
    val resultType: String = "",
    val resultJson: String = "",
    val starttime:Long=0,
    val endtime:Long=0,

    val isMobile: Boolean = false,
    val noAttendanceTime: Double = 0.0,
    val peopleCount: Int = 0,
    val peopleTime: Double = 0.0,
    val percentage: String = "",
    val sleepTime: Double = 0.0
)

