package com.training.graduation.screens.startmeeting

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.isEnabled
import android.provider.Settings
import android.os.Process

class ExamWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var examUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        examUrl = intent.getStringExtra("examUrl") ?: ""
        if (examUrl.isEmpty()) {
            Toast.makeText(this, "Invalid Link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let {
                        if (it.startsWith(examUrl.substringBefore("/", examUrl))) {
                            view?.loadUrl(it)
                        } else {
                            Toast.makeText(this@ExamWebViewActivity, "It is not allowed to open external links during the exam.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            loadUrl(examUrl)
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(webView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ))

            // إضافة زر إنهاء الامتحان (اختياري)
            val finishButton = Button(context).apply {
                text = "Finish the exam"
                setOnClickListener {
                    showExitConfirmationDialog()
                }
            }
            addView(finishButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        setContentView(layout)
        startPinningMode()
    }

    private fun startPinningMode() {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val mode = appOpsManager.checkOpNoThrow(
                OPSTR_LOCK_TASK,
                Process.myUid(),
                packageName
            )

            if (mode != AppOpsManager.MODE_ALLOWED) {
                showPinningInstructions()
            } else {
                startLockTask()
            }
        } else {
            startLockTask()
        }
    }

    companion object {
        private const val OPSTR_LOCK_TASK = "android:lock_task_mode"
    }
    private fun showPinningInstructions() {
        AlertDialog.Builder(this)
            .setTitle("Active Screen Pinning")
            .setMessage("To start the exam, you must enable the screen pinning feature. Go to Settings> Security> Screen Pinning and enable it.")
            .setPositiveButton("Open settings") { _, _ ->
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Activated") { _, _ ->
                startLockTask()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sure")
            .setMessage("Are you sure you want to finish the exam?")
            .setPositiveButton("Yes") { _, _ ->
                stopLockTask()
                finish()
            }
            .setNegativeButton("لا", null)
            .show()
    }
override fun onBackPressed() {
    showExitConfirmationDialog()
    super.onBackPressed()
}
}
