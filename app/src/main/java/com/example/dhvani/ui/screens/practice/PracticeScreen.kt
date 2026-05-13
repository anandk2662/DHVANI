package com.example.dhvani.ui.screens.practice

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dhvani.ml.HandLandmarkerHelper
import com.example.dhvani.ml.PredictionResult
import com.example.dhvani.ui.components.*
import com.example.dhvani.ui.theme.PrimaryGreen
import com.example.dhvani.ui.theme.SuccessGreen
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

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
    val sessionState by viewModel.sessionState.collectAsState()
    val currentDelegate by viewModel.currentDelegate.collectAsState()
    val predictionResult by viewModel.predictionResult.collectAsState()
    val cameraSelector by viewModel.cameraSelector.collectAsState()
    val latestResult by viewModel.latestResult.collectAsState()
    val imageHeight by viewModel.imageHeight.collectAsState()
    val imageWidth by viewModel.imageWidth.collectAsState()
    val rotationDegrees by viewModel.rotationDegrees.collectAsState()
    val isRemoteEnabled by viewModel.isRemoteEnabled.collectAsState()
    val isNetworkBusy by viewModel.isNetworkBusy.collectAsState()
    val lastResponseMsg by viewModel.lastResponseMsg.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
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
            if (selectedSign == null && sessionState is PracticeSessionState.Idle) {
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
                .padding(if (selectedSign == null && sessionState is PracticeSessionState.Idle) padding else PaddingValues(0.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Ambient Background
            if (selectedSign == null && sessionState is PracticeSessionState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(0f, 0f)
                            )
                        )
                )
            }

            when {
                sessionState is PracticeSessionState.Finished -> {
                    PracticeResultView(
                        result = sessionState as PracticeSessionState.Finished,
                        onDone = { viewModel.clearSelection() }
                    )
                }
                selectedSign == null -> {
                    SignSelectionList(
                        signs = encounteredSigns,
                        onSignClick = { sign, isRemote ->
                            if (isRemote != isRemoteEnabled) viewModel.toggleInferenceMode()
                            viewModel.selectSign(sign)
                        },
                        onStartSession = { isRemote ->
                            if (isRemote != isRemoteEnabled) viewModel.toggleInferenceMode()
                            viewModel.startRandomSession()
                        },
                        onBackClick = onBackClick
                    )
                }
                hasCameraPermission -> {
                    CameraPracticeView(
                        sign = selectedSign!!,
                        sessionState = sessionState,
                        delegate = currentDelegate,
                        predictionResult = predictionResult,
                        latestResult = latestResult,
                        imageHeight = imageHeight,
                        imageWidth = imageWidth,
                        rotationDegrees = rotationDegrees,
                        cameraSelector = cameraSelector,
                        isRemoteEnabled = isRemoteEnabled,
                        onToggleRemote = { viewModel.toggleInferenceMode() },
                        onFlipCamera = { viewModel.toggleCamera() },
                        onBackClick = { viewModel.clearSelection() },
                        onToggleDelegate = {
                            viewModel.setDelegate(if (currentDelegate == Delegate.CPU) Delegate.GPU else Delegate.CPU)
                        },
                        isNetworkBusy = isNetworkBusy,
                        lastResponseMsg = lastResponseMsg,
                        viewModel = viewModel
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Camera permission required", color = Color.Gray)
                            Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InferenceModeDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectMode: (Boolean) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(220.dp).background(MaterialTheme.colorScheme.surface)
    ) {
        DropdownMenuItem(
            text = { 
                Column {
                    Text("Local AI", fontWeight = FontWeight.Bold)
                    Text("Offline processing", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            onClick = {
                onSelectMode(false)
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null, tint = PrimaryGreen) }
        )
        DropdownMenuItem(
            text = { 
                Column {
                    Text("Cloud AI", fontWeight = FontWeight.Bold)
                    Text("High precision", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            onClick = {
                onSelectMode(true)
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF673AB7)) }
        )
    }
}

@Composable
fun SignSelectionList(
    signs: List<EncounteredSign>,
    onSignClick: (EncounteredSign, Boolean) -> Unit,
    onStartSession: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ModernPracticeHeader(onBackClick)
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Hero Action Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Challenge", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                        Text("10-Sign Dash", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box {
                            var showSessionDropdown by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showSessionDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("START SESSION", fontWeight = FontWeight.Bold)
                            }
                            
                            InferenceModeDropdown(
                                expanded = showSessionDropdown,
                                onDismissRequest = { showSessionDropdown = false },
                                onSelectMode = { isRemote -> onStartSession(isRemote) }
                            )
                        }
                    }
                    Text("⚡", fontSize = 48.sp)
                }
            }

            Text(
                "Personal Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (signs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No signs collected yet. Start learning!", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(signs) { sign ->
                        PracticeSignCard(sign = sign, onSelectMode = { isRemote -> onSignClick(sign, isRemote) })
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPracticeHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 12.dp, end = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
            Text(
                "AI Practice Lab",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Refine your signing precision",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PracticeSignCard(sign: EncounteredSign, onSelectMode: (Boolean) -> Unit) {
    var showDropdown by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { showDropdown = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imagePath = "file:///android_asset/${sign.assetPath}"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = sign.label,
                        modifier = Modifier.fillMaxSize(0.7f).clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(sign.label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { sign.accuracy },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = PrimaryGreen,
                    trackColor = PrimaryGreen.copy(alpha = 0.1f)
                )
                Text(
                    "${(sign.accuracy * 100).toInt()}% Mastery",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        InferenceModeDropdown(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            onSelectMode = onSelectMode
        )
    }
}

@Composable
fun CameraPracticeView(
    sign: EncounteredSign,
    sessionState: PracticeSessionState,
    delegate: Delegate,
    predictionResult: PredictionResult?,
    latestResult: HandLandmarkerResult?,
    imageHeight: Int,
    imageWidth: Int,
    rotationDegrees: Int,
    cameraSelector: CameraSelector,
    isRemoteEnabled: Boolean,
    onToggleRemote: () -> Unit,
    onFlipCamera: () -> Unit,
    onBackClick: () -> Unit,
    onToggleDelegate: () -> Unit,
    isNetworkBusy: Boolean,
    lastResponseMsg: String?,
    viewModel: PracticeViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { 
        scaleType = PreviewView.ScaleType.FILL_CENTER
    } }
    val overlayView = remember { OverlayView(context, null) }
    
    val helper = remember(delegate) {
        HandLandmarkerHelper(context, delegate, viewModel)
    }

    LaunchedEffect(cameraSelector, delegate) {
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
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        AndroidView(factory = { overlayView }, modifier = Modifier.fillMaxSize())

        // HUD Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            // Network Status Indicator
            if (isRemoteEnabled) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 110.dp, start = 24.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isNetworkBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNetworkBusy) "Sending..." else "Online",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Show last error message if any
                if (lastResponseMsg?.startsWith("Error") == true || lastResponseMsg?.startsWith("HTTP") == true || lastResponseMsg?.startsWith("Network") == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 160.dp),
                        color = Color.Red.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = lastResponseMsg ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Session Info
            if (sessionState is PracticeSessionState.InProgress) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { (sessionState.currentSignIndex + 1) / 10f },
                        modifier = Modifier.width(200.dp).height(8.dp).clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        "Sign ${sessionState.currentSignIndex + 1} of 10",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Target Sign
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(bottom = 200.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val imagePath = "file:///android_asset/${sign.assetPath}"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imagePath).build(),
                        contentDescription = sign.label,
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Text(sign.label.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            // Results HUD
            Column(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth()) {
                val accuracy = predictionResult?.confidence ?: 0f
                val prediction = predictionResult?.prediction ?: "Detecting..."
                val isCorrect = accuracy > 0.7f && prediction.equals(sign.label, ignoreCase = true)

                AnimatedResultCard(prediction = prediction, accuracy = accuracy, isCorrect = isCorrect)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("QUIT")
                    }
                    
                    if (sessionState is PracticeSessionState.InProgress) {
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onToggleRemote() },
                            color = if (isRemoteEnabled) Color(0xFF673AB7) else Color.DarkGray,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(if (isRemoteEnabled) "CLOUD" else "LOCAL", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        GradientButton(text = "NEXT SIGN", onClick = onBackClick, modifier = Modifier.weight(2f).fillMaxHeight())
                    }
                }
            }
        }

        // Top Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Surface(
                modifier = Modifier.clickable { onToggleDelegate() },
                color = if (delegate == Delegate.GPU) PrimaryGreen else Color.DarkGray,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (delegate == Delegate.GPU) Icons.Default.FlashOn else Icons.Default.Memory, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (delegate == Delegate.GPU) "GPU" else "CPU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            IconButton(onClick = onFlipCamera, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
            }
        }
    }
}

@Composable
fun AnimatedResultCard(prediction: String, accuracy: Float, isCorrect: Boolean) {
    val backgroundColor by animateColorAsState(if (isCorrect) SuccessGreen.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.7f), label = "ResultBg")
    val scale by animateFloatAsState(if (isCorrect) 1.05f else 1f, label = "ResultScale")

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isCorrect) "PERFECT!" else if (accuracy > 0.4f) "ALMOST THERE..." else "KEEP TRYING", color = Color.White, fontWeight = FontWeight.Bold)
            Text(prediction, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            if (accuracy > 0) {
                LinearProgressIndicator(progress = { accuracy }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
fun PracticeResultView(result: PracticeSessionState.Finished, onDone: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val infiniteTransition = rememberInfiniteTransition(label = "TrophyGlow")
                val glowSize by infiniteTransition.animateFloat(
                    initialValue = 150f,
                    targetValue = 200f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "Size"
                )
                
                Box(modifier = Modifier.size(glowSize.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape))
                Text("🏆", fontSize = 100.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Session Complete!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = PrimaryGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Mastered ${result.correctCount} / ${result.totalCount} signs",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GradientButton(
                text = "CONTINUE",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }
    }
}
