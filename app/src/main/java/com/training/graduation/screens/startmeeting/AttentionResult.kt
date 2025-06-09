package com.training.graduation.screens.startmeeting

data class AttentionResult(
    val Mobile: Boolean,
    val No_attendance_time: Double,
    val People_count: Int,
    val People_time: Double,
    val Percentage_of_attention: String,
    val Sleep_time: Double
)
