package com.example.dhvani.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.repository.AuthRepository
import com.example.dhvani.gamification.GamificationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    fun checkSession(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Give a little time for splash animation
            delay(1500)
            if (authRepository.isUserLoggedIn()) {
                authRepository.getProfile()
                // Initial streak check
                gamificationEngine.checkAndResetBrokenStreak()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}
