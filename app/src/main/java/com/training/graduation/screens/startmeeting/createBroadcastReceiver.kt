package com.training.graduation.screens.startmeeting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

private val participantManager = ParticipantManager.getInstance()
fun createBroadcastReceiver(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    roomName: String
): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "org.jitsi.meet.CONFERENCE_JOINED" -> {
                    Log.d("MeetingReceiver", "Conference joined")
                    val displayName = intent.getStringExtra("displayName") ?: "Me"

                    participantManager.addParticipant("local-user", displayName)

                    val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putString("currentMeetingId", roomName).apply()

                    if (roomName.isEmpty()) {
                        Log.e("MeetingReceiver", "Room name is empty, cannot add participant to Firebase")
                        return
                    }

                    FirebaseManager.addParticipant(roomName, "local-user", displayName) { success ->
                        if (success) {
                            Log.d("MeetingReceiver", "Participant added to Firebase")
                        } else {
                            Log.e("MeetingReceiver", "Failed to add participant to Firebase")
                        }
                    }
                }

                "org.jitsi.meet.PARTICIPANT_JOINED" -> {
                    Log.d("MeetingReceiver", "Participant joined")
                    val participantId = intent.getStringExtra("id") ?: return
                    val displayName = intent.getStringExtra("displayName") ?: "Unknown"

                    participantManager.addParticipant(participantId, displayName)

                    if (roomName.isEmpty()) {
                        Log.e("MeetingReceiver", "Room name is empty, cannot add participant to Firebase")
                        return
                    }

                    FirebaseManager.addParticipant(roomName, participantId, displayName) { success ->
                        if (success) {
                            Log.d("MeetingReceiver", "Participant added to Firebase")
                        } else {
                            Log.e("MeetingReceiver", "Failed to add participant to Firebase")
                        }
                    }
                }

                "org.jitsi.meet.CONFERENCE_TERMINATED" -> {
                    Log.d("MeetingReceiver", "Conference terminated - creating final report")
                    val currentRoomName = intent.getStringExtra("room") ?: ""

                    FirebaseManager.endMeeting(currentRoomName) { success ->
                        if (success) {
                            Log.d("MeetingReceiver", "Meeting ended in Firebase")
                        } else {
                            Log.e("MeetingReceiver", "Failed to end meeting in Firebase")
                        }
                    }

                    val cameraManager = CameraManager.getInstance(context)
                    cameraManager.createFinalReport(currentRoomName)

                    stopCameraService(context)

                    val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().remove("currentMeetingId").apply()
                }

                "org.jitsi.meet.PARTICIPANT_LEFT" -> {
                    Log.d("MeetingReceiver", "Participant left")
                    val participantId = intent.getStringExtra("id") ?: return

                    participantManager.removeParticipant(participantId)

                    if (roomName.isEmpty()) {
                        Log.e("MeetingReceiver", "Room name is empty, cannot remove participant from Firebase")
                        return
                    }

                    FirebaseManager.removeParticipant(roomName, participantId) { success ->
                        if (success) {
                            Log.d("MeetingReceiver", "Participant removed from Firebase")
                        } else {
                            Log.e("MeetingReceiver", "Failed to remove participant from Firebase")
                        }
                    }
                }
            }
        }
    }

    val filter = IntentFilter().apply {
        addAction("org.jitsi.meet.CONFERENCE_JOINED")
        addAction("org.jitsi.meet.CONFERENCE_TERMINATED")
        addAction("org.jitsi.meet.PARTICIPANT_JOINED")
        addAction("org.jitsi.meet.PARTICIPANT_LEFT")
    }

    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)

    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            }
        }
    })

    return receiver
}


//
//fun createBroadcastReceiver(
//    context: Context,
//    lifecycleOwner: LifecycleOwner,
//    roomName: String
//): BroadcastReceiver {
//    val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                "org.jitsi.meet.CONFERENCE_JOINED" -> {
//                    Log.d("MeetingReceiver", "Conference joined")
//                    val displayName = intent.getStringExtra("displayName") ?: "Me"
//
//                    // إضافة المستخدم الحالي كمشارك محلياً
//                    participantManager.addParticipant("local-user", displayName)
//
//                    // حفظ معرف الميتنج الحالي
//                    val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
//                    sharedPrefs.edit().putString("currentMeetingId", roomName).apply()
//
//                    // إضافة المستخدم الحالي كمشارك في Firebase
//                    FirebaseManager.addParticipant(roomName, "local-user", displayName) { success ->
//                        if (success) {
//                            Log.d("MeetingReceiver", "Participant added to Firebase")
//                        } else {
//                            Log.e("MeetingReceiver", "Failed to add participant to Firebase")
//                        }
//                    }
//                }
//
//                "org.jitsi.meet.PARTICIPANT_JOINED" -> {
//                    Log.d("MeetingReceiver", "Participant joined")
//                    val participantId = intent.getStringExtra("id") ?: return
//                    val displayName = intent.getStringExtra("displayName") ?: "Unknown"
//
//                    // إضافة المشارك محلياً
//                    participantManager.addParticipant(participantId, displayName)
//
//                    // إضافة المشارك في Firebase
//                    FirebaseManager.addParticipant(roomName, participantId, displayName) { success ->
//                        if (success) {
//                            Log.d("MeetingReceiver", "Participant added to Firebase")
//                        } else {
//                            Log.e("MeetingReceiver", "Failed to add participant to Firebase")
//                        }
//                    }
//                }
//
//                "org.jitsi.meet.CONFERENCE_TERMINATED" -> {
//                    Log.d("MeetingReceiver", "Conference terminated - creating final report")
//                    val roomName = intent.getStringExtra("room") ?: "meeting"
//
//                    // إنهاء الميتنج في Firebase
//                    FirebaseManager.endMeeting(roomName) { success ->
//                        if (success) {
//                            Log.d("MeetingReceiver", "Meeting ended in Firebase")
//                        } else {
//                            Log.e("MeetingReceiver", "Failed to end meeting in Firebase")
//                        }
//                    }
//
//                    // إنشاء التقرير النهائي
//                    val cameraManager = CameraManager.getInstance(context)
//                    cameraManager.createFinalReport(roomName)
//
//                    // إيقاف الـ Service
//                    stopCameraService(context)
//
//                    // مسح معرف الميتنج الحالي
//                    val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
//                    sharedPrefs.edit().remove("currentMeetingId").apply()
//                }
//
//                "org.jitsi.meet.PARTICIPANT_LEFT" -> {
//                    Log.d("MeetingReceiver", "Participant left")
//                    val participantId = intent.getStringExtra("id") ?: return
//
//                    // إزالة المشارك محلياً
//                    participantManager.removeParticipant(participantId)
//
//                    // إزالة المشارك من Firebase
//                    FirebaseManager.removeParticipant(roomName, participantId) { success ->
//                        if (success) {
//                            Log.d("MeetingReceiver", "Participant removed from Firebase")
//                        } else {
//                            Log.e("MeetingReceiver", "Failed to remove participant from Firebase")
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    val filter = IntentFilter().apply {
//        addAction("org.jitsi.meet.CONFERENCE_JOINED")
//        addAction("org.jitsi.meet.CONFERENCE_TERMINATED")
//        addAction("org.jitsi.meet.PARTICIPANT_JOINED")
//        addAction("org.jitsi.meet.PARTICIPANT_LEFT")
//    }
//
//    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
//
//    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
//        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_DESTROY) {
//                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
//            }
//        }
//    })
//
//    return receiver
//}


//
//fun createBroadcastReceiver(
//    context: Context,
//    lifecycleOwner: LifecycleOwner,
//    roomName: String
//): BroadcastReceiver {
//
//    val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                "org.jitsi.meet.CONFERENCE_JOINED" -> {
//                    Log.d("MeetingReceiver", "Conference joined")
//                    val displayName = intent.getStringExtra("displayName") ?: "Me"
//                    participantManager.addParticipant("local-user", displayName)
//                    Log.d("MeetingReceiver", "Participant count: ${participantManager.getParticipantCount()}")
//                }
//
//                "org.jitsi.meet.PARTICIPANT_JOINED" -> {
//                    Log.d("MeetingReceiver", "Participant joined")
//                    val participantId = intent.getStringExtra("id") ?: return
//                    val displayName = intent.getStringExtra("displayName") ?: "Unknown"
//                    participantManager.addParticipant(participantId, displayName)
//                }
//                "org.jitsi.meet.CONFERENCE_TERMINATED" -> {
//                    Log.d("MeetingReceiver", "Conference terminated - creating final report")
//                    val roomName = intent.getStringExtra("room") ?: "meeting"
//                    Log.d("MeetingReceiver", "Room name: $roomName")
//                    val cameraManager = CameraManager.getInstance(context)
//                    cameraManager.createFinalReport(roomName)
//                    Log.d("MeetingReceiver", "Final report created, stopping camera service")
//                    stopCameraService(context)
//                }
//
//                "org.jitsi.meet.PARTICIPANT_LEFT" -> {
//                    Log.d("MeetingReceiver", "Participant left")
//                    val participantId = intent.getStringExtra("id") ?: return
//                    participantManager.removeParticipant(participantId)
//                }
//            }
//        }
//    }
//
//    val filter = IntentFilter().apply {
//        addAction("org.jitsi.meet.CONFERENCE_JOINED")
//        addAction("org.jitsi.meet.CONFERENCE_TERMINATED")
//        addAction("org.jitsi.meet.PARTICIPANT_JOINED")
//        addAction("org.jitsi.meet.PARTICIPANT_LEFT")
//    }
//
//    LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
//
//    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
//        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_DESTROY) {
//                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
//            }
//        }
//    })
//
//    return receiver
//}