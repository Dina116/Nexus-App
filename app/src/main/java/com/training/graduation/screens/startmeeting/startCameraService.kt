package com.training.graduation.screens.startmeeting

import android.content.Context
import android.content.Intent
import android.os.Build

fun startCameraService(context: Context, isCheatingDetectionEnabled: Boolean,roomName:String) {
    val intent = Intent(context, CameraService::class.java).apply {
        putExtra("isCheatingDetectionEnabled", isCheatingDetectionEnabled)
        putExtra("meetingName", roomName)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
fun stopCameraService(context: Context) {
    val intent = Intent(context, CameraService::class.java)
    context.stopService(intent)
}