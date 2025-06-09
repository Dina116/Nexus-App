//package com.training.graduation.screens.startmeeting
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.snapshots.SnapshotStateList
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import androidx.navigation.NavController
//import com.google.firebase.auth.FirebaseAuth
//import com.training.graduation.R
//import org.jitsi.meet.sdk.BroadcastEvent
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
//import org.jitsi.meet.sdk.JitsiMeetUserInfo
//import org.jitsi.meet.sdk.JitsiMeetView
//import java.net.URL
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun JitsiMeetCompose(navController: NavController) {
//
//    var roomName by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var roomNameError by remember { mutableStateOf(false) }
//    var passwordError by remember { mutableStateOf(false) }
//    val participants = remember { mutableStateListOf<String>() }
//    val context = LocalContext.current
//    val currentUser = FirebaseAuth.getInstance().currentUser
//    val userName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Guest"
//    val email = currentUser?.email ?: "guest@example.com"
//
//
//    // اعملي receiver هنا
//    val receiver = remember {
//        createJitsiBroadcastReceiver { newList ->
//            participants.clear()
//            participants.addAll(newList)
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val intentFilter = IntentFilter().apply {
//            addAction("org.jitsi.meet.CONFERENCE_JOINED")
//            addAction("org.jitsi.meet.CONFERENCE_TERMINATED")
//            addAction("org.jitsi.meet.PARTICIPANT_JOINED")
//            addAction("org.jitsi.meet.PARTICIPANT_LEFT")
//        }
//        LocalBroadcastManager.getInstance(context)
//            .registerReceiver(receiver, intentFilter)
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            LocalBroadcastManager.getInstance(context)
//                .unregisterReceiver(receiver)
//        }
//    }
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = {
//                Text(
//                    fontWeight = FontWeight.Bold,
//                    text = stringResource(R.string.let_s_start_your_meeting)
//                )
//            })
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            OutlinedTextField(
//                value = roomName,
//                onValueChange = {
//                    roomName = it
//                    roomNameError = it.isBlank()
//                },
//                label = { Text(stringResource(R.string.meeting_name)) },
//                isError = roomNameError,
//                supportingText = {
//                    if (roomNameError) {
//                        Text(
//                            stringResource(R.string.please_enter_meeting_name),
//                            color = Color.Red
//                        )
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = {
//                    password = it
//                    passwordError = it.isBlank()
//                },
//                label = { Text(stringResource(R.string.password)) },
//                isError = passwordError,
//                supportingText = {
//                    if (passwordError) {
//                        Text(
//                            stringResource(R.string.please_enter_password),
//                            color = Color.Red
//                        )
//                    }
//                },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // زر Join Meeting
//            Button(
//                onClick = {
//                    if (roomName.isNotBlank() && password.isNotBlank()) {
//                        roomNameError = false
//                        passwordError = false
//                        val intent = Intent(context, JitsiMeetStandaloneActivity::class.java).apply {
//                            putExtra("room", roomName)
//                            putExtra("userName", userName)
//                            putExtra("email", email)
//                        }
//                        context.startActivity(intent)
//                    } else {
//                        roomNameError = roomName.isBlank()
//                        passwordError = password.isBlank()
//                    }
//
//                },
//
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
//                            start = Offset(0f, 0f),
//                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
//                        ),
//                        shape = RoundedCornerShape(30.dp)
//                    ),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Transparent,
//                    contentColor = Color.White
//                )
//            ) {
//                Text(stringResource(R.string.join_meeting))
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // زر Send Invitation
//            Button(
//                onClick = {
//                    if (roomName.isNotBlank() && password.isNotBlank()) {
//                        sendInvitation(context, roomName, password)
//                    } else {
//                        roomNameError = roomName.isBlank()
//                        passwordError = password.isBlank()
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
//                            start = Offset(0f, 0f),
//                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
//                        ),
//                        shape = RoundedCornerShape(30.dp)
//                    ),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Transparent,
//                    contentColor = Color.White
//                )
//            ) {
//                Text(stringResource(R.string.send_invitation))
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            Text("👥 Participants:", fontWeight = FontWeight.Bold)
//
//            participants.forEach { name ->
//                Text(text = name)
//            }
//        }
//    }
//
//}
//
//fun sendInvitation(context: Context, roomName: String, password: String) {
//    if (roomName.isBlank() || password.isBlank()) return
//
//    val meetingLink = "https://meet.jit.si/$roomName?password=$password"
//    val invitationMessage = context.getString(
//        R.string.you_are_invited_to_join_the_meeting_click_the_link_below,
//        meetingLink
//    )
//
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_TEXT, invitationMessage)
//    }
//
//    context.startActivity(Intent.createChooser(intent, "Send Invitation via"))
//}
//
//val participantMap = mutableMapOf<String, String>()
//
//fun createJitsiBroadcastReceiver(
//    onParticipantsUpdated: (List<String>) -> Unit
//): BroadcastReceiver {
//    return object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent?) {
//            if (intent == null) return
//            val event = BroadcastEvent(intent)
//            when (event.type) {
//                BroadcastEvent.Type.CONFERENCE_JOINED -> {
//                    participantMap.clear()
//                    // ممكن تضيف نفسك في الليستة هنا لو حابة
//                }
//                BroadcastEvent.Type.PARTICIPANT_JOINED -> {
//                    val id = event.data["participantId"]?.toString() ?: return
//                    val name = event.data["name"]?.toString() ?: "Unknown"
//                    participantMap[id] = name
//                    onParticipantsUpdated(participantMap.values.toList())
//                }
//                BroadcastEvent.Type.PARTICIPANT_LEFT -> {
//                    val id = event.data["participantId"]?.toString() ?: return
//                    participantMap.remove(id)
//                    onParticipantsUpdated(participantMap.values.toList())
//                }
//                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
//                    participantMap.clear()
//                    onParticipantsUpdated(participantMap.values.toList())
//                }
//                else -> {}
//            }
//        }
//    }
//}
//
//
//
//
//
