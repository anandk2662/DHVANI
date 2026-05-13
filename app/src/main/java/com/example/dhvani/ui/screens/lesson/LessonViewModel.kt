package com.example.dhvani.ui.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.model.*
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.gamification.GamificationEngine
import com.example.dhvani.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val signRepository: SignRepository,
    private val gamificationEngine: GamificationEngine,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson = _lesson.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted = _isCompleted.asStateFlow()

    private val _canGoNext = MutableStateFlow(false)
    val canGoNext = _canGoNext.asStateFlow()

    fun loadLesson(lessonId: String) {
        val result = signRepository.getLessonById(lessonId)
        _lesson.value = result
        _currentStepIndex.value = 0
        updateCanGoNext()
    }

    fun setStepCompleted(completed: Boolean) {
        _canGoNext.value = completed
        
        // Track the sign as practiced when a step is completed
        val lesson = _lesson.value ?: return
        val step = lesson.steps.getOrNull(_currentStepIndex.value)
        if (completed) {
            viewModelScope.launch {
                val signToTrack = when (step) {
                    is LessonStep.Quiz -> step.sign
                    is LessonStep.Camera -> step.targetSign
                    else -> null
                }
                
                signToTrack?.let { sign ->
                    val practiced = preferences.practicedSigns.toMutableSet()
                    if (practiced.add(sign.id)) {
                        preferences.practicedSigns = practiced
                    }
                }
            }
        }
    }

    fun nextStep() {
        val currentLesson = _lesson.value ?: return
        if (_currentStepIndex.value < currentLesson.steps.size - 1) {
            _currentStepIndex.value++
            updateCanGoNext()
        } else {
            completeLesson()
        }
    }

    private fun updateCanGoNext() {
        val lesson = _lesson.value ?: return
        val step = lesson.steps.getOrNull(_currentStepIndex.value)
        
        // Some steps are auto-completed (like Learn)
        // Others require interaction (Quiz, Match, etc.)
        _canGoNext.value = when (step) {
            is LessonStep.Learn -> true
            else -> false // Requires explicit setStepCompleted(true)
        }
    }

    private fun completeLesson() {
        viewModelScope.launch {
            val currentLesson = _lesson.value ?: return@launch
            val wasCompleted = currentLesson.status == com.example.dhvani.data.model.LessonStatus.COMPLETED
            
            signRepository.completeLesson(currentLesson.id)
            
            if (!wasCompleted) {
                gamificationEngine.onLessonCompleted(perfect = true)
            }

            _isCompleted.value = true
        }
    }
}
