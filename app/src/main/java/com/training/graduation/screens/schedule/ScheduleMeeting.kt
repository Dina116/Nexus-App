package com.training.graduation.screens.schedule


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.training.graduation.R
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale



//@Composable
//@Preview(showSystemUi = true)
//
//fun SchedulePreview(){
//    val navController = NavController(LocalContext.current)
//    ScheduleMeeting(navController)
//}


@Composable
fun ScheduleMeeting(navController:NavController, innerpadding: PaddingValues) {
    var isScheduleVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column( modifier = Modifier.padding(top = 30.dp, start = 10.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
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

        CalendarScreen(onAddEventClick = { isScheduleVisible = true })

            if (isScheduleVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Schedule_Card(onClose = { isScheduleVisible = false })
                }
            }
        }
}

@Composable
fun CalendarScreen(onAddEventClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF000000).copy(alpha = 0.7f),
                        Color(0xFF0D47A1)
                    )
                )
            )
    ) {
        // Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // You can add some header content here
        }
        Spacer(modifier = Modifier.padding(30.dp))
            CalendarView()  // Add your calendar view here if needed

        // Today's Events
        Text(
            text = stringResource(R.string.meetings_scheduled),
            modifier = Modifier.padding(16.dp),
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(listOf("Meeting about KRR", "Team Meeting", "Project Discussion")) { meeting ->
                MeetingCard(meetingName = meeting, description = "Description for $meeting")
            }
        }

        // Floating Action Button
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = colorResource(R.color.orange).copy(alpha = 0.85f),
                contentColor = Color(0xFFFFFFFF),
                modifier = Modifier.padding(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    }
}

@Composable
fun MeetingCard(meetingName: String, description: String) {
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
                    text = "3:00 PM",
                    style = TextStyle(color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                )
            }
        }
    }
}

@Composable
fun CalendarView() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) } // اليوم المحدد
    var currentMonth by remember { mutableStateOf(YearMonth.now()) } // الشهر الحالي

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // الجزء العلوي (الشهر وأزرار التنقل)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "الشهر السابق",
                tint = Color.White,
                modifier = Modifier.clickable {
                    currentMonth = currentMonth.minusMonths(1) // الانتقال للشهر السابق
                }
            )
            Text(
                text = "${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge, // عرض اسم الشهر والسنة
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = "الشهر التالي",
                tint = Color.White,
                modifier = Modifier.clickable {
                    currentMonth = currentMonth.plusMonths(1) // الانتقال للشهر التالي
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // أيام الأسبوع
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(stringResource(R.string.Sun),
                stringResource(R.string.Mon),
                stringResource(R.string.Tue),
                stringResource(R.string.Wed),
                stringResource(R.string.Thu),
                stringResource(R.string.Fri),
                stringResource(R.string.Sat)
            ).forEach { day ->
//                Text(
//                    text = (day),
//                    style = TextStyle(color = Color.White),
//                    style = MaterialTheme.typography.bodyMedium,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.weight(1f)
//                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // عرض الأيام داخل شبكة
        val daysInMonth = (1..currentMonth.lengthOfMonth()).toList()
        val firstDayOfWeek = LocalDate.of(currentMonth.year, currentMonth.month, 1).dayOfWeek.value % 7

        val daysGrid = mutableListOf<String>()
        repeat(firstDayOfWeek) { daysGrid.add("") } // إضافة مسافات فارغة لأيام الأسبوع
        daysGrid.addAll(daysInMonth.map { it.toString() }) // إضافة الأيام

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
                                        currentMonth == YearMonth.of(
                                            selectedDate.year,
                                            selectedDate.month
                                        )
                                    ) Color.White.copy(alpha = 0.15f) else Color.Transparent, // تلوين اليوم المحدد
                                    shape = CircleShape
                                )
                                .clickable(enabled = day.isNotEmpty()) {
                                    selectedDate = LocalDate.of(
                                        currentMonth.year,
                                        currentMonth.month,
                                        day.toInt()
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



