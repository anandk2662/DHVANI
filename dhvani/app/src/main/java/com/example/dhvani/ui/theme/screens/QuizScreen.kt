package com.example.dhvani.ui.theme.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun QuizScreen(navController: NavController, quizId: String?) {
    var currentQuestion by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }

    val questions = listOf(
        "What is this sign?",
        "Another question"
    )
    val total=questions.size
    Column(modifier = Modifier.fillMaxSize().padding(16.dp
    )) {

        Text("Question ${currentQuestion + 1}")

        Spacer(modifier = Modifier.height(20.dp))

        questions.forEachIndexed { index, question ->

            Button(
                onClick = {
                    selectedAnswer = index
                    score++
                }
            ) {
                Text(question)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (currentQuestion < questions.size - 1) {
                currentQuestion++
            } else {
                navController.navigate("result/$total/$score")
            }
        }) {
            Text("Next")
        }
    }
}