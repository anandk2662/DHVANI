package com.example.dhvani.ui.screens.quiz

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ml.HandLandmarkerHelper
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.QuizOptionCard
import com.example.dhvani.ui.theme.PrimaryGreen
import com.example.dhvani.ui.theme.SuccessGreen

@Composable
fun OfflineQuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()
    val timer by viewModel.timer.collectAsState()

    var selectedAnswer by remember { mutableStateOf<com.example.dhvani.data.model.SignItem?>(null) }
    var isCameraActive by remember { mutableStateOf(false) }
    var showDifficultySelection by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(isCameraActive) {
        if (isCameraActive && !hasCameraPermission) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true
            } else {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    if (showDifficultySelection) {
        DifficultySelectionScreen(
            onDifficultySelected = { diff ->
                viewModel.startNewQuiz(difficulty = diff)
                showDifficultySelection = false
            },
            onBack = onBack
        )
    } else if (isFinished) {
        QuizResultScreen(score = score, total = questions.size, onBack = onBack)
    } else if (questions.isNotEmpty()) {
        val currentQuestion = questions[currentIndex]

        Scaffold(
            topBar = {
                QuizTopBar(
                    currentIndex = currentIndex,
                    total = questions.size,
                    timer = timer,
                    onBack = onBack
                )
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
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCameraActive && hasCameraPermission) {
                        QuizCameraPreview(viewModel = viewModel)
                        
                        predictionResult?.let { res ->
                            PredictionOverlay(res)
                        }
                    } else {
                        com.example.dhvani.ui.components.SignCard(
                            sign = currentQuestion.correctAnswer,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                        CameraToggleButton(
                            isActive = isCameraActive,
                            onClick = { isCameraActive = !isCameraActive }
                        )
                    }
                }

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
fun PredictionOverlay(res: com.example.dhvani.ml.PredictionResult) {
    if (res.accuracy > 40) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(60.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Predicted:", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    Text(res.prediction, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                CircularProgressIndicator(
                    progress = { res.accuracy / 100f },
                    modifier = Modifier.size(36.dp),
                    color = if (res.accuracy >= 80) SuccessGreen else Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun CameraToggleButton(isActive: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        Icon(
            if (isActive) Icons.Default.VideocamOff else Icons.Default.Videocam,
            contentDescription = "Toggle Camera",
            tint = Color.White
        )
    }
}


@Composable
fun DifficultySelectionScreen(
    onDifficultySelected: (com.example.dhvani.data.repository.SignRepository.QuizDifficulty) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Difficulty", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        com.example.dhvani.data.repository.SignRepository.QuizDifficulty.values().forEach { difficulty ->
            com.example.dhvani.ui.components.GradientButton(
                text = difficulty.name,
                onClick = { onDifficultySelected(difficulty) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
        
        TextButton(onClick = onBack) {
            Text("Cancel")
        }
    }
}

@Composable
fun QuizTopBar(
    currentIndex: Int,
    total: Int,
    timer: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / total },
            modifier = Modifier.weight(1f).height(12.dp).clip(CircleShape),
            color = if (timer < 10) Color.Red else com.example.dhvani.ui.theme.PrimaryGreen,
            trackColor = com.example.dhvani.ui.theme.PrimaryGreen.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "00:${timer.toString().padStart(2, '0')}",
            fontWeight = FontWeight.Bold,
            color = if (timer < 10) Color.Red else Color.Unspecified
        )
    }
}


@Composable
fun QuizCameraPreview(viewModel: QuizViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    
    val helper = remember {
        HandLandmarkerHelper(context, viewModel)
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        helper.detectLiveStream(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                android.util.Log.e("QuizCameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
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
