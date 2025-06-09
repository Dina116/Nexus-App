package com.training.graduation.screens.startmeeting

data class MeetingData(
    val meetingId: String = "",
    val hostId: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val participants: MutableList<String> = mutableListOf(),
    val isCheatingDetectionEnabled: Boolean = false
)
