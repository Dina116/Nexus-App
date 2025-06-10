package com.training.graduation.onesignal

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun scheduleNotificationFromApp(meetingTitle: String, meetingTimeMillis: Long, playerId: String) {
    val client = OkHttpClient()

    val json = JSONObject().apply {
        put("meetingTitle", meetingTitle)
        put("meetingTime", meetingTimeMillis)
        put("playerId", playerId)
    }
    8
    val body = json.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("https://onsignal-notification-production.up.railway.app/schedule-notification")
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("OneSignal", "❌ Failed to schedule via server", e)
        }

        override fun onResponse(call: Call, response: Response) {
            Log.d("OneSignal", "✅ Server response: ${response.code}")
        }
    })
}
