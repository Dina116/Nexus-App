package com.training.graduation.following

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.jitsi.meet.sdk.BroadcastEvent

class MyJitsiBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val participantMap = mutableMapOf<String, String>()


        val event = BroadcastEvent(intent)
        when (event.type) {
            BroadcastEvent.Type.CONFERENCE_JOINED -> {
                Log.d("JitsiEvent", "✅ Conference joined: ${event.data["url"]}")
            }
            BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                Log.d("JitsiEvent", "❌ Conference terminated: ${event.data["url"]}")
            }
            BroadcastEvent.Type.PARTICIPANT_JOINED -> {
                val id = event.data["participantId"]?.toString() ?: return
                val name = event.data["name"]?.toString() ?: "Unknown"
                participantMap[id] = name
                Log.d("JitsiEvent", "👤 Participant joined: $name ($id)")
                Log.d("JitsiEvent", "👤 Participant joined: ${event.data}")
            }
            BroadcastEvent.Type.PARTICIPANT_LEFT -> {
                val id = event.data["participantId"]?.toString() ?: return
                val name = participantMap[id] ?: "Unknown"
                Log.d("JitsiEvent", "👋 Participant left: $name")
                participantMap.remove(id)
                Log.d("JitsiEvent", "👤 Participant left: ${event.data}")
            }
            BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> {
                Log.d("JitsiEvent", "💬 Chat message: ${event.data["message"]}")
            }
            else -> {
                Log.d("JitsiEvent", "📢 Event received: ${event.type}")
            }
        }
    }
}
