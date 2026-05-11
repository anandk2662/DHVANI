package com.example.dhvani.ui.screens.practice

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.theme.PrimaryGreen
import com.example.dhvani.ui.theme.SuccessGreen

@Composable
fun PracticeScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val encounteredSigns by viewModel.encounteredSigns.collectAsState()
    val selectedSign by viewModel.selectedSign.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()
    val cameraSelector by viewModel.cameraSelector.collectAsState()
    val latestResult by viewModel.latestResult.collectAsState()
    val imageHeight by viewModel.imageHeight.collectAsState()
    val imageWidth by viewModel.imageWidth.collectAsState()
    val rotationDegrees by viewModel.rotationDegrees.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedSign == null) {
                FloatingBottomBar(
                    currentScreen = "practice",
                    onHomeClick = onHomeClick,
                    onPracticeClick = onPracticeClick,
                    onDictionaryClick = onDictionaryClick,
                    onProfileClick = onProfileClick,
                    onLeaderboardClick = onLeaderboardClick
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (selectedSign == null) padding else PaddingValues(0.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedSign == null) {
                SignSelectionList(
                    signs = encounteredSigns,
                    onSignClick = { viewModel.selectSign(it) },
                    onBackClick = onBackClick
                )
            } else if (hasCameraPermission) {
                CameraPracticeView(
                    sign = selectedSign!!,
                    predictionResult = predictionResult,
                    latestResult = latestResult,
                    imageHeight = imageHeight,
                    imageWidth = imageWidth,
                    rotationDegrees = rotationDegrees,
                    cameraSelector = cameraSelector,
                    onFlipCamera = { viewModel.toggleCamera() },
                    onBackClick = { viewModel.clearSelection() },
                    viewModel = viewModel
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SignSelectionList(
    signs: List<EncounteredSign>,
    onSignClick: (EncounteredSign) -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "AI Practice Lab",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Text(
            "Select a sign and use your camera to practice in real-time.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(signs) { sign ->
                AnimatedCard(onClick = { onSignClick(sign) }) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = PrimaryGreen.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(sign.icon, fontSize = 32.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(sign.label, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${(sign.accuracy * 100).toInt()}% Mastery", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPracticeView(
    sign: EncounteredSign,
    predictionResult: com.example.dhvani.ml.PredictionResult?,
    latestResult: com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult?,
    imageHeight: Int,
    imageWidth: Int,
    rotationDegrees: Int,
    cameraSelector: CameraSelector,
    onFlipCamera: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { 
        scaleType = PreviewView.ScaleType.FILL_CENTER
    } }
    val overlayView = remember { com.example.dhvani.ui.components.OverlayView(context, null) }
    
    val helper = remember {
        com.example.dhvani.ml.HandLandmarkerHelper(context, viewModel)
    }

    LaunchedEffect(cameraSelector) {
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
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                android.util.Log.e("CameraPracticeView", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(latestResult) {
        latestResult?.let {
            val isFront = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            overlayView.setResults(it, imageHeight, imageWidth, rotationDegrees, isFront)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { overlayView },
            modifier = Modifier.fillMaxSize()
        )

        // Futuristic HUD Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            // Target Preview Card (Glassmorphic)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sign.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Target: ${sign.label}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Accuracy Ring around the screen (Visual Feedback)
            val accuracy = predictionResult?.confidence ?: 0f
            val accuracyColor by animateColorAsState(
                if (accuracy > 0.8f) SuccessGreen else if (accuracy > 0.4f) Color.Yellow else Color.Red,
                label = "AccuracyColor"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, accuracyColor.copy(alpha = 0.3f))
            )

            // Result Feedback
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                val isCorrect = accuracy > 0.7f
                AnimatedResultCard(
                    prediction = predictionResult?.prediction ?: "Detecting...",
                    accuracy = accuracy,
                    isCorrect = isCorrect
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GradientButton(
                    text = "CHANGE SIGN",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
            }
        }
    }
}

@Composable
fun AnimatedResultCard(prediction: String, accuracy: Float, isCorrect: Boolean) {
    val backgroundColor by animateColorAsState(
        if (isCorrect) SuccessGreen.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.7f),
        label = "ResultColor"
    )
    
    val scale by animateFloatAsState(if (isCorrect) 1.05f else 1f, label = "ResultScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCorrect) "PERFECT!" else if (accuracy > 0.4f) "ALMOST THERE..." else "KEEP TRYING",
                style = MaterialTheme.typography.labelLarge,
                color = if (isCorrect) Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prediction,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            if (accuracy > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { accuracy },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    "Confidence: ${(accuracy * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
