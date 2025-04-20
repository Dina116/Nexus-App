package com.training.graduation.screens.startmeeting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.URL

class JitsiMeetStandaloneActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private lateinit var jitsiView: JitsiMeetView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jitsiView = JitsiMeetView(this)

        val userInfo = JitsiMeetUserInfo().apply {
            displayName = intent.getStringExtra("userName")
            email = intent.getStringExtra("email")
        }

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(intent.getStringExtra("room") ?: "default")
            .setUserInfo(userInfo)
            .setAudioMuted(true)
            .setVideoMuted(true)
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("chat.enabled", true)
            .setFeatureFlag("raise-hand.enabled", true)
            .setFeatureFlag("recording.enabled", true)
            .setFeatureFlag("screen-sharing.enabled", true)
            .setFeatureFlag("tile-view.enabled", true)
            .setFeatureFlag("resolution", 720)
            .setFeatureFlag("prejoinpage.enabled", true)
            .build()

        jitsiView.join(options)
        setContentView(jitsiView)
    }

    override fun onDestroy() {
        jitsiView.dispose()
        super.onDestroy()
    }
    override fun requestPermissions(
        permissions: Array<out String>?,
        requestCode: Int,
        listener: PermissionListener?
    ) {
        listener?.onRequestPermissionsResult(requestCode, permissions, IntArray(permissions?.size ?: 0) { android.content.pm.PackageManager.PERMISSION_GRANTED })
    }

}
