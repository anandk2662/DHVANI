package com.example.dhvani.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.ml.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.camera.core.CameraSelector

data class EncounteredSign(
    val id: String,
    val label: String,
    val icon: String,
    val accuracy: Float = 0f
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val inferenceManager: com.example.dhvani.ml.ModelInferenceManager
) : ViewModel(), HandLandmarkerHelper.LandmarkerListener {

    private val processor = com.example.dhvani.ml.LandmarkProcessor()

    private val _encounteredSigns = MutableStateFlow<List<EncounteredSign>>(
        listOf(
            EncounteredSign("1", "A", "👋"),
            EncounteredSign("2", "B", "🤝"),
            EncounteredSign("3", "C", "👏"),
            EncounteredSign("4", "HELLO", "👋")
        )
    )
    val encounteredSigns = _encounteredSigns.asStateFlow()

    private val _selectedSign = MutableStateFlow<EncounteredSign?>(null)
    val selectedSign = _selectedSign.asStateFlow()

    private val _prediction = MutableStateFlow("Waiting for hand...")
    val prediction = _prediction.asStateFlow()

    private val _accuracy = MutableStateFlow(0f)
    val accuracy = _accuracy.asStateFlow()

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

    fun selectSign(sign: EncounteredSign) {
        _selectedSign.value = sign
    }

    fun clearSelection() {
        _selectedSign.value = null
    }

    fun toggleCamera() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    override fun onError(error: String) {
        viewModelScope.launch {
            _prediction.value = "Error: $error"
        }
    }

    private val _rotationDegrees = MutableStateFlow(0)
    val rotationDegrees = _rotationDegrees.asStateFlow()

    override fun onResults(result: HandLandmarkerResult, imageHeight: Int, imageWidth: Int, rotationDegrees: Int) {
        viewModelScope.launch {
            _latestResult.value = result
            _imageHeight.value = imageHeight
            _imageWidth.value = imageWidth
            _rotationDegrees.value = rotationDegrees
            
            val hands = processor.process(result)
            if (hands.isNotEmpty()) {
                val topHand = hands[0]
                _prediction.value = "Hand Detected (${topHand.handedness})"
                _accuracy.value = topHand.score
                
                // Identify left and right hands for the structured AI request
                val leftHand = hands.find { it.handedness.contains("Left", ignoreCase = true) }?.landmarks
                val rightHand = hands.find { it.handedness.contains("Right", ignoreCase = true) }?.landmarks

                // Use the current lesson's category if available
                // For now, assuming alphabet based on previous context or screen state
                inferenceManager.predictSign(
                    isAlphabet = true, 
                    leftHand = leftHand,
                    rightHand = rightHand
                )
            } else {
                _prediction.value = "Waiting for hand..."
                _accuracy.value = 0f
            }
        }
    }
}
