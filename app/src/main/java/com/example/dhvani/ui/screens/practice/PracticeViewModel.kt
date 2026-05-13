package com.example.dhvani.ui.screens.practice

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.prefs.AppPreferences
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.ml.HandLandmarkerHelper
import com.example.dhvani.ml.ModelInferenceManager
import com.example.dhvani.ml.PredictionResult
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncounteredSign(
    val id: String,
    val label: String,
    val assetPath: String,
    var accuracy: Float = 0f
)

sealed class PracticeSessionState {
    object Idle : PracticeSessionState()
    data class InProgress(val currentSignIndex: Int, val signs: List<EncounteredSign>, val results: List<Boolean>) : PracticeSessionState()
    data class Finished(val correctCount: Int, val totalCount: Int) : PracticeSessionState()
}

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val inferenceManager: ModelInferenceManager,
    private val repository: SignRepository,
    private val preferences: AppPreferences
) : ViewModel(), HandLandmarkerHelper.LandmarkerListener {

    private val _encounteredSigns = MutableStateFlow<List<EncounteredSign>>(emptyList())
    val encounteredSigns = _encounteredSigns.asStateFlow()

    private val _sessionState = MutableStateFlow<PracticeSessionState>(PracticeSessionState.Idle)
    val sessionState = _sessionState.asStateFlow()

    private val _currentDelegate = MutableStateFlow(Delegate.CPU)
    val currentDelegate = _currentDelegate.asStateFlow()

    private val _isProcessingCorrect = MutableStateFlow(false)
    val isProcessingCorrect = _isProcessingCorrect.asStateFlow()

    init {
        loadEncounteredSigns()
        observePredictions()
    }

    private fun observePredictions() {
        viewModelScope.launch {
            inferenceManager.prediction.collectLatest { result ->
                val state = _sessionState.value
                if (state is PracticeSessionState.InProgress && result != null && !_isProcessingCorrect.value) {
                    val targetSign = state.signs[state.currentSignIndex]
                    if (result.confidence > 0.7f && result.prediction.equals(targetSign.label, ignoreCase = true)) {
                        handleCorrectPrediction(targetSign)
                    }
                }
            }
        }
    }

    private fun handleCorrectPrediction(sign: EncounteredSign) {
        viewModelScope.launch {
            _isProcessingCorrect.value = true
            
            // Increase mastery by 5%
            preferences.updateSignMastery(sign.id, 0.05f)
            
            delay(1500) // Success feedback delay
            
            val state = _sessionState.value as? PracticeSessionState.InProgress ?: return@launch
            
            val newResults = state.results + true
            if (state.currentSignIndex < state.signs.size - 1) {
                val nextIndex = state.currentSignIndex + 1
                _sessionState.value = state.copy(
                    currentSignIndex = nextIndex,
                    results = newResults
                )
                _selectedSign.value = state.signs[nextIndex]
            } else {
                _sessionState.value = PracticeSessionState.Finished(
                    correctCount = newResults.count { it },
                    totalCount = state.signs.size
                )
                _selectedSign.value = null
            }
            _isProcessingCorrect.value = false
        }
    }

    fun startRandomSession() {
        val allSigns = repository.getAllSigns().shuffled().take(10).map {
            EncounteredSign(
                id = it.id,
                label = it.label,
                assetPath = it.assetPath,
                accuracy = preferences.getSignMastery(it.id)
            )
        }
        _sessionState.value = PracticeSessionState.InProgress(0, allSigns, emptyList())
        _selectedSign.value = allSigns[0]
    }

    fun setDelegate(delegate: Delegate) {
        _currentDelegate.value = delegate
        // In a real app, we'd need to re-initialize the HandLandmarker with the new delegate
    }

    fun loadEncounteredSigns() {
        val practicedIds = preferences.practicedSigns
        val allSigns = repository.getAllSigns()
        
        val encountered = allSigns.filter { practicedIds.contains(it.id) }
            .map { 
                EncounteredSign(
                    id = it.id,
                    label = it.label,
                    assetPath = it.assetPath,
                    accuracy = preferences.getSignMastery(it.id)
                )
            }
        
        _encounteredSigns.value = encountered
    }

    private val _selectedSign = MutableStateFlow<EncounteredSign?>(null)
    val selectedSign = _selectedSign.asStateFlow()

    private val _predictionResult = inferenceManager.prediction
    val predictionResult = _predictionResult

    private val _latestResult = MutableStateFlow<HandLandmarkerResult?>(null)
    val latestResult = _latestResult.asStateFlow()

    private val _imageHeight = MutableStateFlow(0)
    val imageHeight = _imageHeight.asStateFlow()

    private val _imageWidth = MutableStateFlow(0)
    val imageWidth = _imageWidth.asStateFlow()

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_FRONT_CAMERA)
    val cameraSelector = _cameraSelector.asStateFlow()

    private val _isRemoteEnabled = MutableStateFlow(false)
    val isRemoteEnabled = _isRemoteEnabled.asStateFlow()

    val isNetworkBusy = inferenceManager.isNetworkBusy
    val lastResponseMsg = inferenceManager.lastResponseMsg

    fun toggleInferenceMode() {
        _isRemoteEnabled.value = !_isRemoteEnabled.value
        inferenceManager.clearBuffer()
    }

    fun selectSign(sign: EncounteredSign) {
        _selectedSign.value = sign
    }

    fun clearSelection() {
        _selectedSign.value = null
        _sessionState.value = PracticeSessionState.Idle
    }

    fun toggleCamera() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    override fun onError(error: String) {
        // Handle error
    }

    private val _rotationDegrees = MutableStateFlow(0)
    val rotationDegrees = _rotationDegrees.asStateFlow()

    override fun onResults(result: HandLandmarkerResult, imageHeight: Int, imageWidth: Int, rotationDegrees: Int) {
        viewModelScope.launch {
            _latestResult.value = result
            _imageHeight.value = imageHeight
            _imageWidth.value = imageWidth
            _rotationDegrees.value = rotationDegrees
            
            if (_isRemoteEnabled.value) {
                inferenceManager.predictRemote(result)
            } else {
                inferenceManager.predictOnDevice(result)
            }
        }
    }
}
