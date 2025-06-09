package com.training.graduation.screens.startmeeting

data class ParticipantData(
    val participantId: String = "",
    val displayName: String = "",
    val meetingId: String = "",
    val joinTime: Long = 0,
    val leaveTime: Long = 0
)

