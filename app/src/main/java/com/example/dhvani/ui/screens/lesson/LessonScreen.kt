package com.example.dhvani.ui.screens.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.model.*
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.QuizOptionCard
import com.example.dhvani.ui.components.SignCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonId: String,
    onBack: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val lesson by viewModel.lesson.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val currentLesson = lesson
        if (currentLesson == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val steps = currentLesson.steps
            if (currentStepIndex < steps.size) {
                val currentStep = steps[currentStepIndex]
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1).toFloat() / steps.size },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentStep) {
                            is LessonStep.Learn -> LearnStepView(currentStep.sign)
                            is LessonStep.Quiz -> QuizStepView(currentStep)
                            is LessonStep.Match -> MatchStepView(currentStep)
                            is LessonStep.Camera -> CameraStepView(currentStep)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GradientButton(
                        text = if (currentStepIndex < steps.size - 1) "CONTINUE" else "FINISH",
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun LearnStepView(sign: SignItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("New Sign Learned!", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        SignCard(sign = sign, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun QuizStepView(step: LessonStep.Quiz) {
    var selectedAnswer by remember { mutableStateOf<SignItem?>(null) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Which sign is this?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        SignCard(sign = step.sign, modifier = Modifier.height(240.dp))
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(step.options) { option ->
                QuizOptionCard(
                    sign = option,
                    isSelected = selectedAnswer == option,
                    onClick = { selectedAnswer = option }
                )
            }
        }
    }
}

@Composable
fun MatchStepView(step: LessonStep.Match) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Match the following", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(step.pairs) { pair ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pair.label, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("← matches →", color = Color.Gray)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(pair.sign.label, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CameraStepView(step: LessonStep.Camera) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Show the sign for '${step.targetSign.label}'", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("AI Camera Tracking Active", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tip: Position your hand clearly in frame", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}
