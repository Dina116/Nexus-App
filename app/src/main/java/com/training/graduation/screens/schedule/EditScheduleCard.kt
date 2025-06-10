package com.training.graduation.screens.schedule

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.training.graduation.R
import com.training.graduation.screens.Authentication.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Schedule_Card(onClose: () -> Unit) {
    Edit_schedule(onClose)
}

@Composable
fun Edit_schedule(onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = 80.dp, bottom = 80.dp, start = 20.dp, end = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            ScheduleInputs(onClose)
        }
    }
}

@Composable
fun ScheduleInputs(onClose: () -> Unit) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var meetingName by remember { mutableStateOf("") }
    var meetingDescription by remember { mutableStateOf("") }
    var meetingNameError by remember { mutableStateOf(false) }
    var meetingDescriptionError by remember { mutableStateOf(false) }
    var selectDateError by remember { mutableStateOf(false) }
    var selectedTimeError by remember { mutableStateOf(false) }

    val scheduleViewModel: AuthViewModel = viewModel()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.meeting_title),
            style = TextStyle(fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold),
        )

        OutlinedTextField(
            value = meetingName,
            onValueChange = {
                meetingName = it
                meetingNameError = it.isEmpty()
            },
            shape = RoundedCornerShape(16.dp),
            label = { Text(stringResource(R.string.meeting_name)) },
            isError = meetingNameError ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (meetingNameError) {
                    Text(
                        text = stringResource(R.string.meeting_name_is_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )


        Spacer(modifier = Modifier.padding(top = 25.dp))

        Text(
            stringResource(R.string.date_and_time),
            style = TextStyle(fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold),
        )

        // Date and Time Inputs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Date Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {
                        selectedDate = it
                        selectDateError = it.isEmpty()
                    },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(stringResource(R.string.select_date)) },
                    isError = selectedTimeError,
                    supportingText = {
                        if (selectDateError) {
                            Text(
                                text = stringResource(R.string.date_is_required),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            ShowDatePicker(context) { date ->
                                selectedDate = date
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.select_date)
                            )
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange =
                        {
                            selectedTime=it
                            selectedTimeError=it.isEmpty()
                        },
                    isError = selectedTimeError,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(stringResource(R.string.select_time)) },
                    supportingText = {
                        if (selectedTimeError) {
                            Text(
                                text = stringResource(R.string.time_is_required),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall

                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            ShowTimePicker(context) { time ->
                                selectedTime = time
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "select_time"
                            )
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 25.dp))

        Text(
            stringResource(R.string.meeting_description),
            style = TextStyle(fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold),
        )

        OutlinedTextField(
            value = meetingDescription,
            onValueChange = {
                meetingDescription = it
                meetingDescriptionError = it.isEmpty()
            },
            shape = RoundedCornerShape(16.dp),
            label = { Text(stringResource(R.string.description)) },
            isError = meetingDescriptionError,
            supportingText = {
                if (meetingDescriptionError) {
                    Text(
                        text = stringResource(R.string.description_is_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .padding(vertical = 8.dp),
            maxLines = 4,
            singleLine = false,
        )
        Row {
            Button(onClick = {
                if (meetingName.isBlank()) meetingNameError = true
                if (meetingDescription.isBlank()) meetingDescriptionError = true
                if (selectedDate.isBlank()) selectDateError = true
                if (selectedTime.isBlank()) selectedTimeError = true

                if (!meetingNameError && !meetingDescriptionError && !selectDateError && !selectedTimeError) {
                    val dateTimeString = "$selectedDate $selectedTime"
                    val format = SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a", Locale.getDefault())
                    val meetingDate = format.parse(dateTimeString)

                    meetingDate?.let { date ->
                        val meetingTimeMillis = date.time
                        val meetingLink = "https://meet.jit.si/$meetingName"
                        val currentUserId = Firebase.auth.currentUser?.uid

                        currentUserId?.let { uid ->
                            scheduleViewModel.addMeetingToUserSchedule(
                                userId = uid,
                                title = meetingName,
                                description = meetingDescription,
                                meetingTimeMillis = meetingTimeMillis,
                                meetingLink = meetingLink
                            )
//                            val requestBody = mapOf(
//                                "meetingName" to meetingName,
//                                "meetingLink" to meetingLink,
//                                "meetingTimeMillis" to meetingTimeMillis
//                            )

//                            val requestJson = requestBody.toJsonRequestBody()
//
//                            httpClient.post("https://your-server-url.com/schedule-meeting") {
//                                setBody(requestJson)
//                            }


                        }
                        Toast.makeText(context, "Meeting scheduled successfully", Toast.LENGTH_SHORT).show()
                        onClose()
                    }
                }
            }) {
                Text(stringResource(R.string.Save))
            }
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Button(onClick = onClose) {
                Text(stringResource(R.string.Cancel))
            }
        }
    }
}

// DatePicker Function
fun ShowDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            onDateSelected(dateFormat.format(selectedCalendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// TimePicker Function
fun ShowTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val isPM = hourOfDay >= 12
            val hour12Format = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val selectedTime = String.format(
                "%02d:%02d %s",
                hour12Format,
                minute,
                if (isPM) "PM" else "AM"
            )
            onTimeSelected(selectedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).show()
}

