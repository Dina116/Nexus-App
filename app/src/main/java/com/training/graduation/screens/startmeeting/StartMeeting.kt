package com.training.graduation.screens.startmeeting


import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.facebook.common.file.FileUtils.mkdirs
import com.training.graduation.R
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JitsiMeetCompose(navController: NavController,viewModel: PreMeetingViewModel = viewModel(), innerpadding: PaddingValues) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var roomName by viewModel.roomName
    var password by viewModel.password
    var userName by viewModel.userName
    var roomNameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var userNameError by remember { mutableStateOf(false) }
    var isCheatingDetectionEnabled by remember { mutableStateOf(false)}

    val receiver = remember(context, roomName) {
        createBroadcastReceiver(context, lifecycleOwner, roomName)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("JitsiMeet", "Meeting ended, stopping camera service")
        val sharedPrefs = context.getSharedPreferences("MeetingPrefs", Context.MODE_PRIVATE)
        val currentMeetingId = sharedPrefs.getString("currentMeetingId", "") ?: ""
        val cameraManager = CameraManager.getInstance(context)
        cameraManager.createFinalReport(currentMeetingId)
        stopCameraService(context)
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = stringResource(R.string.let_s_start_your_meeting)
                )
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = roomName,
                onValueChange = {
                    roomName = it
                    roomNameError = it.isBlank()
                },
                label = { Text(stringResource(R.string.meeting_name)) },
                isError = roomNameError,
                supportingText = {
                    if (roomNameError) {
                        Text(stringResource(R.string.please_enter_meeting_name), color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.isBlank()
                },
                label = { Text(stringResource(R.string.password)) },
                isError = passwordError,
                supportingText = {
                    if (passwordError) {
                        Text(stringResource(R.string.please_enter_password), color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    userNameError = it.isBlank()
                },
                label = { Text("Your Name") },
                isError = userNameError,
                supportingText = {
                    if (userNameError) {
                        Text("Please enter your name", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Cheating Detection",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = isCheatingDetectionEnabled,
                    onCheckedChange = { isCheatingDetectionEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    if (roomName.isBlank()) {
                        roomNameError = true
                    } else {
                        roomNameError = false
                    }
                    if (password.isBlank()) {
                        passwordError = true
                    } else {

                    }
                    if (userName.isBlank()) {
                        userNameError = true
                    } else {
                        userNameError = false
                    }

                    if (!roomNameError && !passwordError && !userNameError) {
                        FirebaseManager.createMeeting(roomName, isCheatingDetectionEnabled) { success ->
                            if (success) {
                                Log.d("JitsiMeetCompose", "Meeting created in Firebase")
                                startJitsiMeeting(context, roomName, password, userName, launcher)
                                startCameraService(context, isCheatingDetectionEnabled, roomName)
                            } else {
                                Log.e("JitsiMeetCompose", "Failed to create meeting in Firebase")
                                Toast.makeText(context, "Failed to create meeting", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
                            start = Offset(
                                0f,
                                0f
                            ),
                            end = Offset(
                                Float.POSITIVE_INFINITY,
                                Float.POSITIVE_INFINITY
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White

                ),

                ) {
                Text(stringResource(R.string.join_meeting))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (roomName.isBlank()) {
                        roomNameError = true
                    } else {
                        roomNameError = false
                        sendInvitation(context, roomName, password)
                    }
                    if (password.isBlank()) {
                        passwordError = true
                    } else {
                        passwordError = false
                        sendInvitation(context, roomName, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
                            start = Offset(
                                0f,
                                0f
                            ),
                            end = Offset(
                                Float.POSITIVE_INFINITY,
                                Float.POSITIVE_INFINITY
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White

                ),
                shape = RoundedCornerShape(30.dp)

            ) {
                Text(stringResource(R.string.send_invitation))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("pdf_reports")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("View PDF Reports")
            }
        }
    }

}


fun startJitsiMeeting(
    context: Context,
    roomName: String,
    password: String,
    userName: String,
    launcher: ActivityResultLauncher<Intent>? = null
) {
    if (roomName.isBlank() || password.isBlank()) return
    try {
        val serverURL = URL("https://meet.jit.si/")
        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            .setRoom(roomName)
            .setAudioMuted(true)
            .setVideoMuted(true)
            .setUserInfo(
                JitsiMeetUserInfo().apply { displayName = userName }
            )
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("meeting-password.enabled", true)
            .setFeatureFlag("security-options.enabled", true)
            .setFeatureFlag("conference-timer.enabled", true)
            .setFeatureFlag("pip.enabled", true)
            .setFeatureFlag("chat.enabled", true)
            .setFeatureFlag("screen-sharing.enabled", true)
            .setFeatureFlag("recording.enabled", true)
            .setFeatureFlag("live-streaming.enabled", true)
            .build()

        if (launcher != null) {
            val intent = Intent(context, JitsiMeetActivity::class.java)
            intent.action = "org.jitsi.meet.CONFERENCE"
            intent.putExtra("JitsiMeetConferenceOptions", options)
            launcher.launch(intent)
        } else {
            JitsiMeetActivity.launch(context, options)
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }
}
fun sendInvitation(context: Context, roomName: String, password: String) {
    if (roomName.isBlank() || password.isBlank()) return

    val meetingLink = "https://meet.jit.si/$roomName?password=$password"
    val invitationMessage = context.getString(
        R.string.you_are_invited_to_join_the_meeting_click_the_link_below,
        meetingLink
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, invitationMessage)
    }

    context.startActivity(Intent.createChooser(intent, "Send Invitation via"))
}