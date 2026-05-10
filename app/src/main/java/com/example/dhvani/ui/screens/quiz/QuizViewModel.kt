package com.example.dhvani.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.model.QuizQuestion
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.gamification.GamificationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: SignRepository,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    init {
        startNewQuiz()
    }

    fun startNewQuiz() {
        _questions.value = repository.getRandomQuiz()
        _currentQuestionIndex.value = 0
        _score.value = 0
        _isFinished.value = false
    }

    fun submitAnswer(answer: SignItem) {
        val currentQuestion = _questions.value[_currentQuestionIndex.value]
        if (answer == currentQuestion.correctAnswer) {
            _score.value++
            viewModelScope.launch {
                gamificationEngine.onCorrectAnswer()
            }
        }

        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value++
        } else {
            _isFinished.value = true
            viewModelScope.launch {
                gamificationEngine.onQuizCompleted(perfect = _score.value == _questions.value.size)
            }
        }
    }
}
