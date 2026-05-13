package com.example.dhvani.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.PremiumTextField
import com.example.dhvani.ui.theme.WelcomeGradient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Decoration
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
            Text(
                "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp).padding(top = 100.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login to your account",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Forgot Password?",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator()
                    } else {
                        GradientButton(
                            text = "LOGIN",
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            " OR ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { 
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("876950550227-3ddpl8uaoj4mm5evidv5ina6uk5gmhrf.apps.googleusercontent.com")
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val credentialManager = CredentialManager.create(context)
                            
                            scope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        request = request,
                                        context = context
                                    )
                                    val credential = result.credential
                                    
                                    // Handle the credential type properly
                                    try {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        viewModel.signInWithCredentialManager(googleIdTokenCredential.idToken)
                                    } catch (e: Exception) {
                                        android.util.Log.e("Login", "Failed to parse Google ID Token: ${e.message}")
                                        viewModel.setError("Sign-in failed: Could not process Google account data.")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("Login", "Credential Manager failed: ${e.message}")
                                    val errorMessage = when {
                                        e.message?.contains("No credentials available") == true -> 
                                            "No Google accounts found for this app. Check if SHA-1 and Client ID match in Google Console."
                                        else -> e.message ?: "Google Sign-In failed"
                                    }
                                    viewModel.setError(errorMessage)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text("Sign in with Google")
                    }

                    if (authState is AuthState.Error && (authState as AuthState.Error).message.contains("Check if SHA-1")) {
                        TextButton(
                            onClick = { viewModel.signInWithGoogleNative() }
                        ) {
                            Text("Try Browser-based Sign-in instead", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        TextButton(
            onClick = onSignupClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "Don't have an account? Sign Up",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
