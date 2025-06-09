package com.training.graduation.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.training.graduation.R
import com.training.graduation.screens.Authentication.AuthViewModel
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

data class Meeting(
    val title: String = "",
    val description: String = "",
    val meetingTime: Long = 0L,
    val meetingLink: String = ""
)

@Preview(showSystemUi = true)
@Composable
fun SchedulePreview() {
    val navController = NavController(LocalContext.current)
    ScheduleMeeting(navController)
}

@Composable
fun ScheduleMeeting(navController: NavController) {
    var isScheduleVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var reloadTrigger by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(top = 30.dp, start = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    stringResource(R.string.schedule_meetings),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
            }
        }

        CalendarScreen(
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            onAddEventClick = { isScheduleVisible = true },
            onForceReload = { reloadTrigger = !reloadTrigger },
            reloadKey = reloadTrigger
        )

        if (isScheduleVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Schedule_Card(onClose = {
                    isScheduleVisible = false
                    reloadTrigger = !reloadTrigger
                })
            }
        }
    }
}

@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onAddEventClick: () -> Unit,
    onForceReload: () -> Unit,
    reloadKey: Boolean
) {
    val viewModel = remember { AuthViewModel() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }

    LaunchedEffect(reloadKey) {
        viewModel.getUserScheduledMeetings(
            userId = userId,
            onResult = { fetched ->
                meetings = fetched.map {
                    Meeting(
                        title = it["title"].toString(),
                        description = it["description"].toString(),
                        meetingTime = (it["meetingTime"] as? Long) ?: 0L,
                        meetingLink = it["meetingLink"].toString()
                    )
                }
            },
            onError = { e -> println("❌ Error fetching meetings: ${e.message}") }
        )
    }

    val filteredMeetings = meetings.filter {
        val meetingDate = Instant.ofEpochMilli(it.meetingTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        meetingDate == selectedDate
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF000000).copy(alpha = 0.7f), Color(0xFF0D47A1))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // مساحة للزرار
        ) {
            Spacer(modifier = Modifier.padding(30.dp))
            CalendarView(selectedDate = selectedDate, onDateSelected = onDateChange)

            Text(
                text = stringResource(R.string.meetings_scheduled),
                modifier = Modifier.padding(16.dp),
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(filteredMeetings) { meeting ->
                    MeetingCard(
                        meetingName = meeting.title,
                        description = meeting.description,
                        time = meeting.meetingTime
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddEventClick,
            containerColor = colorResource(R.color.orange).copy(alpha = 0.85f),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Event")
        }
    }
}

@Composable
fun MeetingCard(meetingName: String, description: String, time: Long) {
    val formattedTime = remember(time) {
        val date = Date(time)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        format.format(date)
    }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meetingName,
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = description,
                    style = TextStyle(color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = formattedTime,
                    style = TextStyle(color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Previous Month",
                tint = Color.White,
                modifier = Modifier.clickable { currentMonth = currentMonth.minusMonths(1) }
            )
            Text(
                text = "${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = "Next Month",
                tint = Color.White,
                modifier = Modifier.clickable { currentMonth = currentMonth.plusMonths(1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val daysInMonth = (1..currentMonth.lengthOfMonth()).toList()
        val firstDayOfWeek = LocalDate.of(currentMonth.year, currentMonth.month, 1).dayOfWeek.value % 7
        val daysGrid = mutableListOf<String>()
        repeat(firstDayOfWeek) { daysGrid.add("") }
        daysGrid.addAll(daysInMonth.map { it.toString() })

        Column {
            daysGrid.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (day.isNotEmpty() && day.toInt() == selectedDate.dayOfMonth &&
                                        currentMonth == YearMonth.of(selectedDate.year, selectedDate.month)
                                    ) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable(enabled = day.isNotEmpty()) {
                                    onDateSelected(
                                        LocalDate.of(currentMonth.year, currentMonth.month, day.toInt())
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (day.isNotEmpty()) Color.White else Color.Transparent
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
