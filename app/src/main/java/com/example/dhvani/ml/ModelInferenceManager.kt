package com.example.dhvani.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PredictionResult(
    val prediction: String,
    val confidence: Float,
    val accuracy: Int,
    val status: String
)

@Singleton
class ModelInferenceManager @Inject constructor(
    private val handModelInference: HandModelInference
) {
    private val _prediction = MutableStateFlow<PredictionResult?>(null)
    val prediction = _prediction.asStateFlow()

    private val featureExtractor = FeatureExtractor()

    // Majority voting buffer for stabilization
    private val predictionBuffer = mutableListOf<String>()
    private val BUFFER_SIZE = 15 // Increased for better stability
    
    // Cooldown to prevent accidental double-submissions
    private var lastAcceptedTime = 0L
    private val COOLDOWN_MS = 2000L

    /**
     * Complete on-device inference using TFLite and local feature extraction.
     */
    fun predictOnDevice(result: HandLandmarkerResult) {
        val features = featureExtractor.extractFeatures(result)
        
        // Hand Detection Validation: Check presence flags at index 0 and 27
        val leftHandPresent = features[0] == 1f
        val rightHandPresent = features[27] == 1f

        if (!leftHandPresent && !rightHandPresent) {
            _prediction.value = PredictionResult(
                prediction = "No Hand Detected",
                confidence = 0f,
                accuracy = 0,
                status = "GUIDANCE: Place your hand in the frame"
            )
            predictionBuffer.clear()
            return
        }

        val (predictedLabel, confidence) = handModelInference.runInference(features)

        // Smoothing: Add to buffer
        predictionBuffer.add(predictedLabel)
        if (predictionBuffer.size > BUFFER_SIZE) {
            predictionBuffer.removeAt(0)
        }

        // Majority Voting
        val smoothedPrediction = predictionBuffer
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: predictedLabel

        // Debounce & Confidence Threshold
        val isStable = predictionBuffer.count { it == smoothedPrediction } > (BUFFER_SIZE * 0.7)
        val finalConfidence = if (isStable) confidence else confidence * 0.5f

        _prediction.value = PredictionResult(
            prediction = smoothedPrediction,
            confidence = finalConfidence,
            accuracy = (finalConfidence * 100).toInt(),
            status = when {
                finalConfidence >= 0.8f -> "STABLE"
                finalConfidence >= 0.5f -> "HOLD STEADY"
                else -> "ADJUST HAND"
            }
        )
    }

    fun canSubmitPrediction(label: String): Boolean {
        val current = _prediction.value ?: return false
        val now = System.currentTimeMillis()
        
        return current.prediction.equals(label, ignoreCase = true) && 
               current.accuracy >= 80 && 
               (now - lastAcceptedTime) > COOLDOWN_MS
    }

    fun markAccepted() {
        lastAcceptedTime = System.currentTimeMillis()
        predictionBuffer.clear()
    }
}
