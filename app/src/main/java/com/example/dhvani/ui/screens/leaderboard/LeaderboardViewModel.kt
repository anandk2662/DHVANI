package com.example.dhvani.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.repository.AuthRepository
import com.example.dhvani.data.repository.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val currentUser = authRepository.currentUserProfile

    init {
        // Initial fetch for whatever the default is, 
        // will be updated by Screen when it detects current user's league
        fetchLeaderboard()
    }

    fun fetchLeaderboard(leagueId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getLeaderboard(leagueId)
            _users.value = result
            _isLoading.value = false
        }
    }
}
