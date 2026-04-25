package com.example.dhvani.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.TokenManager
import com.example.dhvani.model.AuthResponse
import com.example.dhvani.model.User
import com.example.dhvani.repository.AuthRepository
import kotlinx.coroutines.launch


class AuthViewModel(application: Application): AndroidViewModel(application){

    private val repository= AuthRepository(application)
    private val tokenManager = TokenManager(application)

    var loginResult by mutableStateOf<AuthResponse?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var registerResult by mutableStateOf<User?>(null)
        private set

    fun login(email: String,password: String){
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = repository.login(email, password)
                Log.d("AuthViewModel", "Login Result: $result")
                
                if (result != null) {
                    if (result.accessToken != null) {
                        tokenManager.saveTokens(result.accessToken, result.refreshToken)
                        loginResult = result
                    } else {
                        errorMessage = "Server error: No access token received. Response: $result"
                    }
                } else {
                    errorMessage = "Login failed: Invalid credentials or empty response"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage ?: "Failed to connect to server"}"
                e.printStackTrace()
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.getAccessToken() != null
    }

    fun logout() {
        tokenManager.clearTokens()
        loginResult = null
    }

    fun clearError() {
        errorMessage = null
    }
}