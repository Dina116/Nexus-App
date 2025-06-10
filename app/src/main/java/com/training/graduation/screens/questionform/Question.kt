package com.training.graduation.screens.questionform

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class Question(val id: Int, val text: String)

suspend fun readQuestionsFromCsv(context: Context, fileName: String): List<Question> {
    return withContext(Dispatchers.IO) {
        val questions = mutableListOf<Question>()
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        var id = 1

        while (reader.readLine().also { line = it } != null) {
            val questionText = line?.trim() ?: ""
            if (questionText.isNotBlank()) {
                questions.add(Question(id, questionText))
                id++
            }
        }

        reader.close()
        inputStream.close()
        questions
    }
}

suspend fun readCorrectAnswers(context: Context, fileName: String): Map<Int, String> {
    return withContext(Dispatchers.IO) {
        val answers = mutableMapOf<Int, String>()
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        var id = 1

        while (reader.readLine().also { line = it } != null) {
            val rawAnswer = line?.trim() ?: ""
            if (rawAnswer.isNotBlank()) {
                // إزالة رقم السؤال من البداية لو موجود
                val cleanedAnswer = rawAnswer.replace(Regex("^\\d+[\\.|\\)]?\\s*"), "")
                answers[id] = cleanedAnswer
                id++
            }
        }

        reader.close()
        inputStream.close()
        answers
    }
}

@Composable
fun QuestionFormScreen(navController: NavController) {
    val context = LocalContext.current

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    val answers = remember { mutableStateMapOf<Int, String>() }
    var correctAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val resultMap = remember { mutableStateMapOf<Int, Boolean>() }

    var userName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            questions = readQuestionsFromCsv(context, "Questions_bank (7).csv")
            correctAnswers = readCorrectAnswers(context, "Answers (5).csv")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ في تحميل الملفات", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TextField لكتابة اسم المستخدم
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            label = { Text("your name") },
            placeholder = { Text("Enter your name") }
        )

        if (userName.isNotBlank()) {
            Text(
                text = "Hello، $userName 👋",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(questions) { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.text,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TextField(
                            value = answers[question.id] ?: "",
                            onValueChange = {
                                answers[question.id] = it
                                resultMap.remove(question.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("write your answer here...") }
                        )

                        resultMap[question.id]?.let { isCorrect ->
                            Column(modifier = Modifier.padding(top = 6.dp)) {
                                Text(
                                    text = if (isCorrect) "✔️ Correct answer" else "❌Wrong answer",
                                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )

                                if (!isCorrect) {
                                    Text(
                                        text = "the correct Answer: ${correctAnswers[question.id] ?: "غير متوفرة"}",
                                        color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                var count = 0
                for ((id, userAnswer) in answers) {
                    val correctAnswer = correctAnswers[id]?.trim()?.lowercase()
                    val userClean = userAnswer.trim().lowercase()

                    val isCorrect = userClean == correctAnswer
                    resultMap[id] = isCorrect

                    if (isCorrect) count++
                }
                correctCount = count
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.CenterHorizontally),
            enabled = userName.isNotBlank()
        ) {
            Text("summit", style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (showDialog) {
        val total = questions.size
        val percentage = (correctCount.toFloat() / total) * 100
        val evaluation = when {
            percentage >= 90 -> "✅ Excellent"
            percentage >= 60 -> "🙂 good"
            else -> "❗️you must revision this section"
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("ok")
                }
            },
            title = {
                Text(text = "final Evaluation $userName", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    Text("you answer $correctCount from $total correctly✅")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("your final Evaluation: $evaluation")
                }
            }
        )
    }
}