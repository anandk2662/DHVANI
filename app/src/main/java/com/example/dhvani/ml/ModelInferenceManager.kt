package com.example.dhvani.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.example.dhvani.data.prefs.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PredictionResult(
    val prediction: String,
    val confidence: Float,
    val status: String,
    val isStable: Boolean = false
)

@Singleton
class ModelInferenceManager @Inject constructor(
    private val handModelInference: HandModelInference,
    private val preferences: AppPreferences
) {
    private val _prediction = MutableStateFlow<PredictionResult?>(null)
    val prediction = _prediction.asStateFlow()

    private val _isNetworkBusy = MutableStateFlow(false)
    val isNetworkBusy = _isNetworkBusy.asStateFlow()

    private val _lastResponseMsg = MutableStateFlow<String?>(null)
    val lastResponseMsg = _lastResponseMsg.asStateFlow()

    private val featureExtractor = FeatureExtractor()
    
    private var remoteService: RemoteInferenceService? = null
    private var currentBaseUrl: String? = null

    // Stabilization Config
    private val BUFFER_SIZE = 12
    private val CONFIDENCE_THRESHOLD = 0.80f
    private val STABILITY_THRESHOLD = 0.75f // 75% of buffer must match
    
    private val predictionBuffer = ArrayDeque<Pair<String, Float>>(BUFFER_SIZE)
    private val isProcessingRemote = AtomicBoolean(false)

    private fun getRemoteService(): RemoteInferenceService? {
        val url = preferences.aiModelUrl
        if (url.isEmpty()) return null
        
        // Ensure URL ends with / for Retrofit
        val baseUrl = if (url.endsWith("/")) url else "$url/"
        
        if (baseUrl == currentBaseUrl && remoteService != null) {
            return remoteService
        }

        return try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val json = Json { ignoreUnknownKeys = true }
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(client)
                .build()

            currentBaseUrl = baseUrl
            remoteService = retrofit.create(RemoteInferenceService::class.java)
            remoteService
        } catch (e: Exception) {
            android.util.Log.e("ModelInferenceManager", "Failed to create Retrofit service", e)
            null
        }
    }

    /**
     * Complete on-device inference using TFLite and local feature extraction.
     */
    fun predictOnDevice(result: HandLandmarkerResult) {
        val features = featureExtractor.extractFeatures(result)
        
        // Check presence flags (index 0 for left, 27 for right)
        val leftHandPresent = features[0] == 1f
        val rightHandPresent = features[27] == 1f

        if (!leftHandPresent && !rightHandPresent) {
            _prediction.value = PredictionResult(
                prediction = "No Hand Detected",
                confidence = 0f,
                status = "PLACE HAND IN FRAME"
            )
            predictionBuffer.clear()
            return
        }

        val (rawLabel, rawConfidence) = handModelInference.runInference(features)

        // Add to rolling buffer
        predictionBuffer.addLast(rawLabel to rawConfidence)
        if (predictionBuffer.size > BUFFER_SIZE) {
            predictionBuffer.removeFirst()
        }

        // 1. Majority Voting
        val majorityLabel = predictionBuffer
            .groupingBy { it.first }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: rawLabel

        // 2. Stability Check
        val occurrences = predictionBuffer.count { it.first == majorityLabel }
        val stabilityScore = occurrences.toFloat() / predictionBuffer.size
        val isStable = stabilityScore >= STABILITY_THRESHOLD

        // 3. Confidence Smoothing (Average confidence of the majority label in buffer)
        val avgConfidence = predictionBuffer
            .filter { it.first == majorityLabel }
            .map { it.second }
            .average()
            .toFloat()

        val finalConfidence = if (isStable) avgConfidence else avgConfidence * stabilityScore

        _prediction.value = PredictionResult(
            prediction = majorityLabel,
            confidence = finalConfidence,
            isStable = isStable && finalConfidence >= CONFIDENCE_THRESHOLD,
            status = when {
                !isStable -> "HOLD STEADY"
                finalConfidence < CONFIDENCE_THRESHOLD -> "GET CLOSER / ALIGN"
                else -> "STABLE"
            }
        )
        
        // Debug Logging
        if (isStable) {
            android.util.Log.d("Inference", "Prediction: $majorityLabel ($finalConfidence)")
        }
    }

    /**
     * Sends coordinates to remote server for inference.
     */
    fun predictRemote(result: HandLandmarkerResult) {
        if (isProcessingRemote.get()) return
        
        val service = getRemoteService() ?: run {
            _prediction.value = PredictionResult("Error", 0f, "Invalid API URL")
            return
        }

        val leftHandPoints = mutableListOf<List<Float>>()
        val rightHandPoints = mutableListOf<List<Float>>()

        result.handedness().forEachIndexed { index, handednessList ->
            val label = handednessList.firstOrNull()?.categoryName() ?: ""
            val landmarks = result.landmarks()[index]
            val points = landmarks.mapIndexed { i, landmark ->
                listOf(i.toFloat(), landmark.x(), landmark.y())
            }
            
            if (label == "Left") {
                leftHandPoints.addAll(points)
            } else if (label == "Right") {
                rightHandPoints.addAll(points)
            }
        }

        if (leftHandPoints.isEmpty() && rightHandPoints.isEmpty()) {
            _prediction.value = PredictionResult("No Hand", 0f, "PLACE HAND IN FRAME")
            return
        }

        val request = SignInferenceRequest(
            landmarks = LandmarkContainer(Left = leftHandPoints, Right = rightHandPoints)
        )

        // Run in background
        _isNetworkBusy.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("ModelInferenceManager", "Sending remote request to: $currentBaseUrl")
                val response = service.predictSign(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    _lastResponseMsg.value = "Success: ${body?.message}"
                    if (body?.status == 200 && body.data != null) {
                        val confidence = body.data.confidence
                        val prediction = if (confidence >= 0.7f) body.data.sign else "Unknown"
                        
                        _prediction.value = PredictionResult(
                            prediction = prediction,
                            confidence = confidence,
                            status = if (confidence >= 0.7f) "STABLE" else "LOW CONFIDENCE",
                            isStable = confidence >= 0.7f
                        )
                    } else {
                        _prediction.value = PredictionResult("Error", 0f, body?.message ?: "Server Error")
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    _lastResponseMsg.value = errorMsg
                    _prediction.value = PredictionResult("Error", 0f, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network Failed: ${e.localizedMessage}"
                _lastResponseMsg.value = errorMsg
                _prediction.value = PredictionResult("Error", 0f, "Network Failed")
                android.util.Log.e("ModelInferenceManager", "Remote inference failed", e)
            } finally {
                _isNetworkBusy.value = false
                isProcessingRemote.set(false)
            }
        }
    }

    fun clearBuffer() {
        predictionBuffer.clear()
        _prediction.value = null
    }

    /**
     * Checks if the current stable prediction matches the expected label.
     */
    fun canSubmitPrediction(expectedLabel: String): Boolean {
        val current = _prediction.value ?: return false
        return current.isStable && current.prediction.equals(expectedLabel, ignoreCase = true)
    }

    /**
     * Resets the buffer after a prediction has been accepted.
     */
    fun markAccepted() {
        clearBuffer()
    }
}

