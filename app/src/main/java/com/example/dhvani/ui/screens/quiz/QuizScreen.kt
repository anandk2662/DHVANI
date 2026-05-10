package com.example.dhvani.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.theme.PrimaryGreen

@Composable
fun QuizScreen(
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var currentQuestion by remember { mutableIntStateOf(1) }
    val totalQuestions = 5
    val progress = currentQuestion.toFloat() / totalQuestions

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(CircleShape),
                    color = PrimaryGreen,
                    trackColor = PrimaryGreen.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("💎 12", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Select the correct sign for:",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            Text(
                "HELLO",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                QuizOption("👋", "Option A")
                QuizOption("🤝", "Option B")
                QuizOption("👏", "Option C")
            }

            Spacer(modifier = Modifier.weight(1f))

            GradientButton(
                text = if (currentQuestion < totalQuestions) "CHECK" else "FINISH",
                onClick = {
                    if (currentQuestion < totalQuestions) {
                        currentQuestion++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuizOption(icon: String, label: String) {
    var isSelected by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { isSelected = !isSelected },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) PrimaryGreen else Color.LightGray.copy(alpha = 0.3f)
        ),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}
