package com.example.dhvani.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.model.QuizQuestion
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.gamification.GamificationEngine
import com.example.dhvani.data.prefs.AppPreferences
import com.example.dhvani.ml.ModelInferenceManager
import com.example.dhvani.ml.PredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: SignRepository,
    private val gamificationEngine: GamificationEngine,
    private val preferences: AppPreferences,
    private val inferenceManager: ModelInferenceManager
) : ViewModel(), com.example.dhvani.ml.HandLandmarkerHelper.LandmarkerListener {

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    // Camera Support
    private val _predictionResult = inferenceManager.prediction
    val predictionResult = _predictionResult

    private val _latestResult = MutableStateFlow<com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult?>(null)
    val latestResult = _latestResult.asStateFlow()

    private val _imageHeight = MutableStateFlow(0)
    val imageHeight = _imageHeight.asStateFlow()

    private val _imageWidth = MutableStateFlow(0)
    val imageWidth = _imageWidth.asStateFlow()

    private val _rotationDegrees = MutableStateFlow(0)
    val rotationDegrees = _rotationDegrees.asStateFlow()

    init {
        // Default startup if needed, but usually triggered by UI
        // startNewQuiz()
        observePredictions()
    }

    private fun observePredictions() {
        viewModelScope.launch {
            predictionResult.collectLatest { result ->
                val currentQuestion = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return@collectLatest
                
                if (inferenceManager.canSubmitPrediction(currentQuestion.correctAnswer.label)) {
                    inferenceManager.markAccepted()
                    submitAnswer(currentQuestion.correctAnswer)
                }
            }
        }
    }

    override fun onResults(
        result: com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        rotationDegrees: Int
    ) {
        viewModelScope.launch {
            _latestResult.value = result
            _imageHeight.value = imageHeight
            _imageWidth.value = imageWidth
            _rotationDegrees.value = rotationDegrees
            
            inferenceManager.predictRemote(result)
        }
    }

    override fun onError(error: String) {
        // Handle camera error
    }

    private val _timer = MutableStateFlow(30)
    val timer = _timer.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null
    private var currentQuizId: String? = null

    fun startNewQuiz(category: SignCategory = SignCategory.ALPHABET, difficulty: SignRepository.QuizDifficulty = SignRepository.QuizDifficulty.MEDIUM) {
        currentQuizId = "quiz_${category.name}_${difficulty.name}"
        _questions.value = repository.generateQuiz(category, count = 10, difficulty = difficulty)
        _currentQuestionIndex.value = 0
        _score.value = 0
        _isFinished.value = false
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timer.value = 30
        timerJob = viewModelScope.launch {
            while (_timer.value > 0 && !_isFinished.value) {
                kotlinx.coroutines.delay(1000)
                _timer.value--
            }
            if (_timer.value == 0) {
                finishQuiz()
            }
        }
    }

    fun submitAnswer(answer: SignItem) {
        val currentQuestion = _questions.value[_currentQuestionIndex.value]
        if (answer == currentQuestion.correctAnswer) {
            _score.value++
            viewModelScope.launch {
                val isSolved = preferences.completedQuizzes.contains(currentQuizId)
                if (!isSolved) {
                    gamificationEngine.onCorrectAnswer()
                }
                
                // Add to practiced signs
                val practiced = preferences.practicedSigns.toMutableSet()
                practiced.add(answer.id)
                preferences.practicedSigns = practiced
            }
        }

        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value++
            _timer.value = 30 // Reset timer for next question
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        _isFinished.value = true
        timerJob?.cancel()
        viewModelScope.launch {
            val isSolved = preferences.completedQuizzes.contains(currentQuizId)
            if (!isSolved) {
                gamificationEngine.onQuizCompleted(perfect = _score.value == _questions.value.size)
                
                // Mark as solved if score is decent (e.g. > 70%)
                if (_score.value >= _questions.value.size * 0.7) {
                    currentQuizId?.let { id ->
                        val solved = preferences.completedQuizzes.toMutableSet()
                        solved.add(id)
                        preferences.completedQuizzes = solved
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
