package com.example.dhvani.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Temporary storage for signup details to be used after OTP verification
    private var pendingSignupDetails: SignupDetails? = null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signIn(email, password)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun signup(details: SignupDetails) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signUp(details.email, details.password)
                pendingSignupDetails = details
                _authState.value = AuthState.OtpRequired(details.email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun verifyOtp(email: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.verifyEmailOtp(email, code)
                
                // If we have pending details, create the profile
                val details = pendingSignupDetails
                val user = repository.getCurrentUser()
                
                if (details != null && user != null) {
                    repository.createInitialProfile(
                        userId = user.id,
                        username = details.username,
                        fullName = details.fullName,
                        region = details.region
                    )
                }
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            try {
                repository.resendOtp(email)
                // Optional: You could set a 'Code Sent' state to show a toast/message
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun signInWithCredentialManager(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signInWithIdToken(idToken)
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun signInWithGoogleNative() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signInWithGoogle()
                // The browser will handle the rest; Success is handled by session listeners
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }
    
    private fun mapError(e: Exception): String {
        return when (e) {
            is AuthRepository.UserAlreadyExistsException -> e.message ?: "User already exists"
            else -> {
                val message = e.message ?: "An unexpected error occurred"
                if (message.contains("Invalid login credentials", ignoreCase = true)) {
                    "Invalid email or password"
                } else if (message.contains("Email not confirmed", ignoreCase = true)) {
                    "Please confirm your email before logging in"
                } else if (message.contains("rate limit", ignoreCase = true)) {
                    "Too many requests. Please try again later."
                } else {
                    message
                }
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }
}

data class SignupDetails(
    val email: String,
    val password: String,
    val username: String,
    val fullName: String,
    val region: String
)

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data object ExternalAuthTriggered : AuthState()
    data class OtpRequired(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
