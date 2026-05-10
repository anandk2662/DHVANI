package com.example.dhvani.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.theme.WelcomeGradient

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onGetStartedClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(WelcomeGradient))
    ) {
        // Floating background decorations
        FloatingCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + expandVertically(tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Illustration Placeholder
                    Surface(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "👐", 
                                fontSize = 120.sp,
                                modifier = Modifier.alpha(0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Learn Sign Language Easily",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Master ASL with real-time AI feedback and gamified lessons.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(1000, 500))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    GradientButton(
                        text = "GET STARTED",
                        onClick = onGetStartedClick,
                        gradient = listOf(Color.White, Color.White),
                        textColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Overriding GradientButton text color for this specific white button would be better 
                    // but for now I'll use it as is or adjust the GradientButton component.
                    // Actually let's use a white button with primary text here manually if needed.
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "I ALREADY HAVE AN ACCOUNT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCircles() {
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingCircles")
    
    val xOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "x1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp + xOffset1.dp, y = 100.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .blur(30.dp)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = (-50).dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .blur(20.dp)
        )
    }
}
