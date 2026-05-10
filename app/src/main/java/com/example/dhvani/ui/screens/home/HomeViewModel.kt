package com.example.dhvani.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.dhvani.data.model.Lesson
import com.example.dhvani.data.repository.AuthRepository
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.data.repository.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val signRepository: SignRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    val lessons: StateFlow<List<Lesson>> = signRepository.lessons
    val userProfile: StateFlow<UserProfile?> = authRepository.currentUserProfile
}
