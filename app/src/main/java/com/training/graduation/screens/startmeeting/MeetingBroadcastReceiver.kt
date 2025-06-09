//package com.training.graduation.screens.startmeeting
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.util.Log
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//
//class MeetingBroadcastReceiver(private val context: Context) : BroadcastReceiver() {
//    private val participantManager = ParticipantManager.getInstance()
//
//    override fun onReceive(context: Context, intent: Intent) {
//        when (intent.action) {
//            "org.jitsi.meet.CONFERENCE_JOINED" -> {
//                Log.d("MeetingReceiver", "Conference joined")
//                val displayName = intent.getStringExtra("displayName") ?: "Me"
//                participantManager.addParticipant("local-user", displayName)
//            }
//            "org.jitsi.meet.CONFERENCE_TERMINATED" -> {
//                Log.d("MeetingReceiver", "Conference terminated")
//                val roomName = intent.getStringExtra("room") ?: "meeting"
//                val cameraManager = CameraManager.getInstance(context)
//                cameraManager.createFinalReport(roomName)
//                stopCameraService(context)
//            }
//            "org.jitsi.meet.PARTICIPANT_JOINED" -> {
//                Log.d("MeetingReceiver", "Participant joined")
//                val participantId = intent.getStringExtra("id") ?: return
//                val displayName = intent.getStringExtra("displayName") ?: "Unknown"
//                participantManager.addParticipant(participantId, displayName)
//            }
//            "org.jitsi.meet.PARTICIPANT_LEFT" -> {
//                Log.d("MeetingReceiver", "Participant left")
//                val participantId = intent.getStringExtra("id") ?: return
//                participantManager.removeParticipant(participantId)
//            }
//        }
//    }
//
//    fun register() {
//        val filter = IntentFilter().apply {
//            addAction("org.jitsi.meet.CONFERENCE_JOINED")
//            addAction("org.jitsi.meet.CONFERENCE_TERMINATED")
//            addAction("org.jitsi.meet.PARTICIPANT_JOINED")
//            addAction("org.jitsi.meet.PARTICIPANT_LEFT")
//        }
//        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter)
//    }
//
//    fun unregister() {
//        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
//    }
//}