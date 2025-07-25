package com.training.graduation.uploadvideo

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.training.graduation.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun VideoResultScreen() {
    val context = LocalContext.current

    val summaryFileName = "The_summary_English.txt"
    val notesFileName = "Main_Notes_English.txt"
    val videoFileName = "uploaded_video.mp4"

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val videoFile = File(downloadsDir, videoFileName)

    val downloadedFiles = remember { mutableStateListOf<File>() }

    val isOnline = remember { mutableStateOf(true) }

    // التحقق من الإنترنت عند فتح الشاشة
    LaunchedEffect(Unit) {
        isOnline.value = isInternetAvailable(context)
        if (!isOnline.value) {
            Toast.makeText(context, "لا يوجد اتصال بالإنترنت حاليًا", Toast.LENGTH_LONG).show()
        }
    }

    fun handleDownload(fileName: String) {
        val file = downloadAssetFile(context, fileName)
        file?.let {
            if (!downloadedFiles.contains(it)) {
                downloadedFiles.add(it)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Video Uploaded Successfully", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_video),
                        contentDescription = "Video Icon",
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(text = videoFile.name, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isOnline.value) {
                Button(
                    onClick = { handleDownload(summaryFileName) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Download Summary")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { handleDownload(notesFileName) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Download Notes")
                }
            } else {
                Text(
                    text = "⚠️ Files cannot be downloaded without an internet connection",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DownloadedFilesList(files = downloadedFiles)
        }
    }
}

fun downloadAssetFile(context: Context, fileName: String): File? {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outFile = File(downloadsDir, fileName)

        if (outFile.exists()) {
            outFile.delete()
        }

        val assetManager = context.assets
        val inputStream = assetManager.open(fileName)
        val outputStream = FileOutputStream(outFile)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        Toast.makeText(context, "$fileName downloaded", Toast.LENGTH_SHORT).show()
        outFile
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error downloading $fileName", Toast.LENGTH_SHORT).show()
        null
    }
}

fun openFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "text/plain")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(Intent.createChooser(intent, "Open file with"))
}

@Composable
fun DownloadedFilesList(files: List<File>) {
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(files) { file ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                onClick = { openFile(context, file) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = file.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
