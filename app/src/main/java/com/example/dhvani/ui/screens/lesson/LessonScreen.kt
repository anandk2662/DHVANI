package com.example.dhvani.ui.screens.lesson

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.model.*
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.QuizOptionCard
import com.example.dhvani.ui.components.SignCard
import com.example.dhvani.ui.theme.PrimaryGreen
import com.example.dhvani.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

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
    val canGoNext by viewModel.canGoNext.collectAsState()

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
                title = { Text(lesson?.title ?: "Loading...", fontWeight = FontWeight.Bold) },
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
                    // Modern Progress Bar
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.2f))) {
                        val progress = (currentStepIndex + 1).toFloat() / steps.size
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(Brush.linearGradient(listOf(PrimaryGreen, SuccessGreen)))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
                            },
                            label = "StepTransition"
                        ) { step ->
                            when (step) {
                                is LessonStep.Learn -> LearnStepView(step.sign)
                                is LessonStep.Quiz -> QuizStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                                is LessonStep.Match -> MatchStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                                is LessonStep.Camera -> CameraStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                                is LessonStep.FillBlank -> FillBlankStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                                is LessonStep.Rearrange -> RearrangeStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                                is LessonStep.TimedChallenge -> TimedChallengeStepView(step, onComplete = { viewModel.setStepCompleted(it) })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GradientButton(
                        text = if (currentStepIndex < steps.size - 1) "CONTINUE" else "FINISH",
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canGoNext
                    )
                }
            }
        }
    }
}

@Composable
fun LearnStepView(sign: SignItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("New Sign Learned!", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Text(sign.label, fontSize = 64.sp, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(2.dp))
        SignCard(sign = sign, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun QuizStepView(step: LessonStep.Quiz, onComplete: (Boolean) -> Unit) {
    var selectedAnswer by remember { mutableStateOf<SignItem?>(null) }
    var wrongAnswer by remember { mutableStateOf<SignItem?>(null) }

    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null && selectedAnswer == step.sign) {
            delay(300) // brief pause so user sees selection before moving on
            onComplete(true)
        } else if (selectedAnswer != null && selectedAnswer != step.sign) {
            wrongAnswer = selectedAnswer
            selectedAnswer = null
            onComplete(false)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Which letter represents this sign?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(130.dp))
        SignCard(sign = step.sign, modifier = Modifier.height(140.dp))
        Spacer(modifier = Modifier.height(120.dp))

        val fourOptions = remember(step) {
            val correct = step.sign
            // Use the options provided in the step, but ensure we have 4 items
            // previously it was filtering only alphabets/numbers which broke word quizes
            val options = step.options.toMutableList()
            if (!options.contains(correct)) options.add(correct)
            options.distinctBy { it.id }.shuffled().take(4)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(fourOptions) { option ->
                QuizOptionCard(
                    sign = option,
                    isSelected = selectedAnswer == option,
                    isWrong = wrongAnswer == option,
                    onClick = {
                        if (wrongAnswer != option) {  // prevent re-selecting a wrong answer
                            wrongAnswer = null
                            selectedAnswer = option
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MatchStepView(step: LessonStep.Match, onComplete: (Boolean) -> Unit) {
    var selectedLeft by remember { mutableStateOf<MatchPair?>(null) }
    var selectedRight by remember { mutableStateOf<MatchPair?>(null) }
    var matches by remember { mutableStateOf(setOf<Pair<MatchPair, MatchPair>>()) }
    var wrongLeft by remember { mutableStateOf<MatchPair?>(null) }
    var wrongRight by remember { mutableStateOf<MatchPair?>(null) }


    LaunchedEffect(selectedLeft, selectedRight) {
        if (selectedLeft != null && selectedRight != null) {
            if (selectedLeft!!.label == selectedRight!!.label) {
                matches = matches + (selectedLeft!! to selectedRight!!)
                selectedLeft = null
                selectedRight = null
            } else {
                wrongLeft = selectedLeft
                wrongRight = selectedRight
                delay(600)
                wrongLeft = null
                wrongRight = null
                selectedLeft = null
                selectedRight = null
            }
        }
    }
    LaunchedEffect(matches) {
        if (matches.size == step.pairs.size) {
            onComplete(true)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Match the pairs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                step.pairs.forEach { pair ->
                    val isMatched = matches.any { it.first == pair }
                    MatchCard(
                        text = pair.sign.label,
                        isSelected = selectedLeft == pair,
                        isMatched = isMatched,
                        isWrong = wrongLeft == pair,
                        onClick = { if (!isMatched) selectedLeft = pair }
                    )
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                step.pairs.shuffled(java.util.Random(42)).forEach { pair ->
                    val isMatched = matches.any { it.second == pair }
                    val isSelected = selectedRight == pair
                    val isWrong = wrongRight == pair
                    Surface(
                        onClick = {
                            if (!isMatched) selectedRight = if (isSelected) null else pair
                        },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(20.dp),

                        color = when {
                            isMatched -> SuccessGreen.copy(alpha = 0.1f)
                            isWrong -> Color.Red.copy(alpha = 0.1f)
                            isSelected -> PrimaryGreen.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(2.dp, when {
                            isMatched -> SuccessGreen
                            isWrong -> Color.Red
                            isSelected -> PrimaryGreen
                            else -> Color.LightGray.copy(alpha = 0.3f)
                        })
                    ) {
                        SignCard(
                            sign = pair.sign,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(text: String, isSelected: Boolean, isMatched: Boolean, isWrong: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isMatched -> SuccessGreen.copy(alpha = 0.1f)
            isWrong -> Color.Red.copy(alpha = 0.1f)
            isSelected -> PrimaryGreen.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = when {
                isMatched -> SuccessGreen
                isWrong -> Color.Red.copy(alpha = 0.1f)
                isSelected -> PrimaryGreen
                else -> Color.LightGray.copy(alpha = 0.3f)
            }
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (isMatched) {
                Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp))
            }
        }
    }
}

@Composable
fun FillBlankStepView(step: LessonStep.FillBlank, onComplete: (Boolean) -> Unit) {
    var selectedOption by remember { mutableStateOf<SignItem?>(null) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Complete the sentence", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Text(
                text = step.sentence.replace("[BLANK]", if (selectedOption != null) " __${selectedOption!!.label}__ " else " _______ "),
                modifier = Modifier.padding(32.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            step.options.forEach { option ->
                QuizOptionCard(
                    sign = option,
                    isSelected = selectedOption == option,
                    onClick = { 
                        selectedOption = option
                        onComplete(true)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RearrangeStepView(step: LessonStep.Rearrange, onComplete: (Boolean) -> Unit) {
    var orderedWords by remember { mutableStateOf(listOf<String>()) }
    val remainingWords = step.scrambledWords.filter { !orderedWords.contains(it) }

    LaunchedEffect(orderedWords) {
        if (orderedWords.size == step.scrambledWords.size) {
            onComplete(true)
        } else {
            onComplete(false)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rearrange to match", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Target Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(2.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            FlowRow (
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                orderedWords.forEach { word ->
                    WordChip(word) { orderedWords = (orderedWords - word) }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Options Area
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            remainingWords.forEach { word ->
                WordChip(word) { orderedWords = (orderedWords + word) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordChip(word: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Text(word, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CameraStepView(step: LessonStep.Camera, onComplete: (Boolean) -> Unit) {
    // Simulate AI detection
    LaunchedEffect(Unit) {
        delay(3000)
        onComplete(true)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("AI Practice: Show the sign", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            SignCard(sign = step.targetSign, modifier = Modifier.size(200.dp))
            
            // Hud Overlay Sim
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Box(modifier = Modifier.align(Alignment.TopEnd).size(60.dp).border(2.dp, PrimaryGreen, CircleShape)) {
                    Text("98%", color = PrimaryGreen, modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(step.hint ?: "Position your hand in the center of the frame", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TimedChallengeStepView(step: LessonStep.TimedChallenge, onComplete: (Boolean) -> Unit) {
    var timeLeft by remember { mutableIntStateOf(step.timeLimitSeconds) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onComplete(true)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⏱️", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "00:${timeLeft.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (timeLeft < 10) Color.Red else Color.Unspecified
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Rapid Recognition!", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        
        SignCard(sign = step.signs.first(), modifier = Modifier.fillMaxSize())
    }
}
