package com.example.dhvani.ui.screens.practice

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dhvani.data.prefs.AppPreferences
import com.example.dhvani.data.repository.SignRepository
import com.example.dhvani.ml.HandLandmarkerHelper
import com.example.dhvani.ml.ModelInferenceManager
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncounteredSign(
    val id: String,
    val label: String,
    val assetPath: String,
    val accuracy: Float = 0f
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val inferenceManager: ModelInferenceManager,
    private val repository: SignRepository,
    private val preferences: AppPreferences
) : ViewModel(), HandLandmarkerHelper.LandmarkerListener {

    private val _encounteredSigns = MutableStateFlow<List<EncounteredSign>>(emptyList())
    val encounteredSigns = _encounteredSigns.asStateFlow()

    init {
        loadEncounteredSigns()
    }

    fun loadEncounteredSigns() {
        val practicedIds = preferences.practicedSigns
        val allSigns = repository.getAllSigns()
        
        val encountered = allSigns.filter { practicedIds.contains(it.id) }
            .map { 
                EncounteredSign(
                    id = it.id,
                    label = it.label,
                    assetPath = it.assetPath
                )
            }
        
        _encounteredSigns.value = encountered
    }

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
            
            // On-Device Inference
            inferenceManager.predictOnDevice(result)
        }
    }
}
