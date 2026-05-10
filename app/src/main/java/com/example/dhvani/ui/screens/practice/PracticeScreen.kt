package com.example.dhvani.ui.screens.practice

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
                "Practice Signs",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            "Select a sign you've encountered to practice with AI",
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
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.LightGray.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sign.icon, fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(sign.label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
            overlayView.setResults(it, imageHeight, imageWidth, isFront)
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

        // Real-time Hand Position Display
        latestResult?.landmarks()?.getOrNull(0)?.let { landmarks ->
            val wrist = landmarks[0]
            val indexFinger = landmarks[8]
            
            Surface(
                modifier = Modifier.padding(top = 80.dp, start = 24.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("LIVE TRACKING", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wrist: [X: ${"%.2f".format(wrist.x())}, Y: ${"%.2f".format(wrist.y())}]",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Index: [X: ${"%.2f".format(indexFinger.x())}, Y: ${"%.2f".format(indexFinger.y())}]",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Frame: ${imageWidth}x${imageHeight}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // ... rest of the UI

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sign.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Target: ${sign.label}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            val isCorrect = (predictionResult?.confidence ?: 0f) > 0.7f
            AnimatedResultCard(
                prediction = predictionResult?.prediction ?: "Detecting...",
                accuracy = predictionResult?.confidence ?: 0f,
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
}

@Composable
fun AnimatedResultCard(prediction: String, accuracy: Float, isCorrect: Boolean) {
    val backgroundColor by animateColorAsState(
        if (isCorrect) SuccessGreen.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f),
        label = "ResultColor"
    )
    
    val scale by animateFloatAsState(if (isCorrect) 1.05f else 1f, label = "ResultScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCorrect) "EXCELLENT!" else "DETECTING...",
                style = MaterialTheme.typography.labelLarge,
                color = if (isCorrect) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prediction,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCorrect) Color.White else Color.Black
            )
            
            if (accuracy > 0) {
                LinearProgressIndicator(
                    progress = { accuracy },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isCorrect) Color.White else SuccessGreen,
                    trackColor = (if (isCorrect) Color.White else SuccessGreen).copy(alpha = 0.2f)
                )
                Text(
                    "Accuracy: ${(accuracy * 100).toInt()}%",
                    color = if (isCorrect) Color.White else Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
