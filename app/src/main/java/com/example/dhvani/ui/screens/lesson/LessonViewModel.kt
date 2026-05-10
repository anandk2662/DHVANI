package com.example.dhvani.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.model.*
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.gamification.GamificationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val signRepository: SignRepository,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson = _lesson.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted = _isCompleted.asStateFlow()

    fun loadLesson(lessonId: String) {
        val result = signRepository.getLessonById(lessonId)
        _lesson.value = result
        _currentStepIndex.value = 0
    }

    fun nextStep() {
        val currentLesson = _lesson.value ?: return
        if (_currentStepIndex.value < currentLesson.steps.size - 1) {
            _currentStepIndex.value++
        } else {
            completeLesson()
        }
    }

    private fun completeLesson() {
        viewModelScope.launch {
            val currentLesson = _lesson.value ?: return@launch
            signRepository.completeLesson(currentLesson.id)
            gamificationEngine.onLessonCompleted(perfect = true)
            _isCompleted.value = true
        }
    }
}
