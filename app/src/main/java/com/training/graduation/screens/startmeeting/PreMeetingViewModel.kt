package com.training.graduation.screens.startmeeting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PreMeetingViewModel : ViewModel() {
    var roomName = mutableStateOf("")
    var password = mutableStateOf("")
    var userName = mutableStateOf("")
    var isCheatingDetectionEnabled = mutableStateOf(false)
}