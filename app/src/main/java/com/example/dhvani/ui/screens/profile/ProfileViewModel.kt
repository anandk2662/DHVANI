package com.example.dhvani.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.repository.AuthRepository
import com.example.dhvani.data.repository.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: com.example.dhvani.data.prefs.AppPreferences
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = authRepository.currentUserProfile

    private val _aiModelUrl = kotlinx.coroutines.flow.MutableStateFlow(preferences.aiModelUrl)
    val aiModelUrl = _aiModelUrl.asStateFlow()

    fun updateAiUrl(url: String) {
        preferences.aiModelUrl = url
        _aiModelUrl.value = url
    }

    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        fetchProfile()
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                authRepository.getProfile()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(username: String, fullName: String, region: String) {
        val current = profile.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val updated = current.copy(
                    username = username,
                    full_name = fullName,
                    region = region
                )
                authRepository.updateProfile(updated)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSuccess()
        }
    }
}

// Add missing extension
fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.asStateFlow() = this
