package com.example.dhvani.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.QuizOptionCard
import com.example.dhvani.ui.theme.PrimaryGreen

@Composable
fun OfflineQuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()

    var selectedAnswer by remember { mutableStateOf<com.example.dhvani.data.model.SignItem?>(null) }

    if (isFinished) {
        QuizResultScreen(score = score, total = questions.size, onBack = onBack)
    } else if (questions.isNotEmpty()) {
        val currentQuestion = questions[currentIndex]

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size },
                        modifier = Modifier.weight(1f).height(12.dp),
                        color = PrimaryGreen,
                        trackColor = PrimaryGreen.copy(alpha = 0.1f)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Which sign is this?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                com.example.dhvani.ui.components.SignCard(
                    sign = currentQuestion.correctAnswer,
                    modifier = Modifier.height(240.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(currentQuestion.options) { option ->
                        QuizOptionCard(
                            sign = option,
                            isSelected = selectedAnswer == option,
                            onClick = { selectedAnswer = option }
                        )
                    }
                }

                GradientButton(
                    text = "SUBMIT",
                    onClick = {
                        selectedAnswer?.let {
                            viewModel.submitAnswer(it)
                            selectedAnswer = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    enabled = selectedAnswer != null
                )
            }
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, total: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 80.sp)
        Text("Quiz Completed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "You scored $score out of $total",
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryGreen
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "CONTINUE",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
