package com.example.dhvani.ui.theme.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dhvani.R

@Composable
fun WelcomeScreen(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4FF),
                        Color(0xFFF5FAFF),
                        Color.White
                    )
                )
            )
    ) {
        // Decorative background circle
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-30).dp, y = 100.dp)
                .background(Color(0xFFBBDEFB).copy(alpha = 0.4f), CircleShape)
        )

        val infiniteTransition = rememberInfiniteTransition(label = "imageFloating")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -20f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Image Section
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = offsetY.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle for image
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.Center)
                )
                
                Image(
                    painter = painterResource(id = R.drawable.name),
                    contentDescription = "Welcome Image",
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                )

            }

            // Text and Buttons Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Learn Sign Language Easily",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    color = Color(0xFF1A237E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Communicate with confidence through our interactive lessons and real-time practice.",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { navController.navigate("choice") }, // Using "login" as destination
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Text(
                        "I already have an account", 
                        color = Color(0xFF1A237E),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            
                // Page indicator (visual only)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .background(Color(0xFF2196F3), RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.LightGray.copy(alpha = 0.6f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.LightGray.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}
