package com.example.dhvani.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.PremiumTextField
import com.example.dhvani.ui.theme.WelcomeGradient

@Composable
fun OtpVerificationScreen(
    email: String,
    onVerificationSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onVerificationSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(
                    Brush.verticalGradient(WelcomeGradient),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Verify Email", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Enter the 8-digit code sent to",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    PremiumTextField(
                        value = code,
                        onValueChange = { if (it.length <= 8) code = it },
                        label = "Verification Code",
                        leadingIcon = Icons.Default.LockOpen,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    
                    if (authState is AuthState.Error) {
                        Text(
                            (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    GradientButton(
                        text = "VERIFY",
                        onClick = { viewModel.verifyOtp(email, code) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = code.length >= 6 && authState !is AuthState.Loading
                    )
                    
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 24.dp)
        ) {
            Text("Back")
        }

        TextButton(
            onClick = { viewModel.resendOtp(email) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 24.dp)
        ) {
            Text("Resend Code", fontWeight = FontWeight.Bold)
        }
    }
}
