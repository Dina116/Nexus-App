package com.training.graduation.screens.startmeeting

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner

class CameraService : Service() {
    private lateinit var cameraManager: CameraManager
    private val localBinder = LocalBinder()
    private var isCheatingDetectionEnabled = false
    private var meetingName = "meeting"

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }
    override fun onCreate() {
        super.onCreate()
        cameraManager = CameraManager.getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            isCheatingDetectionEnabled = it.getBooleanExtra("isCheatingDetectionEnabled", false)
            cameraManager.setCheatingDetectionEnabled(isCheatingDetectionEnabled)

            meetingName = it.getStringExtra("meetingName") ?: "meeting"
            Log.d("CameraService", "Meeting name set to: $meetingName")
        }
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        val lifecycleOwner = ProcessLifecycleOwner.get()
        cameraManager.startCamera(lifecycleOwner) {
            cameraManager.startImageCaptureLoop()
        }
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder {
        return localBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.createFinalReport(meetingName)
        cameraManager.release()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Camera Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Meeting Camera")
            .setContentText("Camera is active for meeting monitoring")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
    }
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "CameraServiceChannel"
    }
}