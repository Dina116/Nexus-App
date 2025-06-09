package com.training.graduation.screens.startmeeting

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReportsScreen(navController: NavController , innerpadding: PaddingValues) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager.getInstance(context) }
    val reports = remember { mutableStateOf<List<File>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val reportToDelete = remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        reports.value = cameraManager.getPdfReportsList()
    }
    if (showDeleteDialog.value && reportToDelete.value != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog.value = false
                reportToDelete.value = null
            },
            title = { Text("Are you sure?") },
            text = { Text("Are you sure you want to delete the report: ${reportToDelete.value?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    reportToDelete.value?.let { file ->
                        if (file.delete()) {
                            scope.launch {
                                reports.value = cameraManager.getPdfReportsList()
                                Toast.makeText(context, "Pdf deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete the pdf ", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showDeleteDialog.value = false
                    reportToDelete.value = null
                }) {
                        Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog.value = false
                    reportToDelete.value = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (reports.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No reports available")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(reports.value) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        cameraManager.openPdfReport(file)
                                    }
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(file.lastModified())}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(
                                onClick = {
                                    reportToDelete.value = file
                                    showDeleteDialog.value = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
