package com.training.graduation.screens.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.training.graduation.R
import com.training.graduation.screens.Authentication.AuthViewModel
import com.training.graduation.screens.schedule.Meeting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        viewModel.getUserScheduledMeetings(
            userId = userId,
            onResult = { fetched ->
                val now = System.currentTimeMillis()
                val cutoff = now + 2 * 60 * 60 * 1000

                meetings = fetched.map {
                    Meeting(
                        title = it["title"].toString(),
                        description = it["description"].toString(),
                        meetingTime = (it["meetingTime"] as? Long) ?: 0L,
                        meetingLink = it["meetingLink"].toString()
                    )
                }.filter {
                    it.meetingTime in now..cutoff
                }

                isLoading = false
            },
            onError = { e ->
                println("❌ Error fetching notifications: ${e.message}")
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 30.dp, start = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                stringResource(R.string.notifications),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )
        }

        if (!isLoading && meetings.isEmpty()) {
            No_Notification()
        } else if (meetings.isNotEmpty()) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(meetings) { meeting ->
                    NotificationCard(meeting)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(meeting: Meeting) {
    val (formattedDate, formattedTime) = remember(meeting.meetingTime) {
        val date = Date(meeting.meetingTime)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        dateFormat.format(date) to timeFormat.format(date)
    }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meeting.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meeting.description,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formattedDate,
                fontSize = 13.sp,
                color = Color(0xFF424242)
            )
            Text(
                text = formattedTime,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}



@Composable
fun No_Notification(){
    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){



        val image = painterResource(id = R.drawable.notification_)

        Image(
            painter = image,
            contentDescription = "Circular Image",
            modifier = Modifier.size(300.dp)

        )
        Text(stringResource(R.string.you_don_t_have_any_notifications)
            , style = TextStyle(fontWeight=FontWeight.Bold, fontSize = 20.sp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    val navController = rememberNavController()

    NotificationScreen(navController = navController)
}

