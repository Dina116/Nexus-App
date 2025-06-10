package com.training.graduation.uploadvideo


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.training.graduation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UploadVideoScreen(navController: NavController) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it
            isUploading = true
            simulateUpload(
                onProgress = { value -> progress = value },
                onComplete = {
                    isUploading = false
                    navController.navigate("video_result_screen")
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Uploading...", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth(0.7f))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Upload your video", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.upload_icon),
                    contentDescription = "Upload Video",
                    modifier = Modifier
                        .size(200.dp)
                        .clickable { videoPickerLauncher.launch("video/*") }
                )
            }
        }
    }
}

fun simulateUpload(onProgress: (Float) -> Unit, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.Default).launch {
        val totalSteps = 100
        for (i in 1..totalSteps) {
            delay(30)
            withContext(Dispatchers.Main) {
                onProgress(i / totalSteps.toFloat())
            }
        }
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }
}
