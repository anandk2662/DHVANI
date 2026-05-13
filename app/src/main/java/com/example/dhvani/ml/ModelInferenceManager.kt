package com.example.dhvani.ml

import android.util.Log
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
    private val stabilizer = PredictionStabilizer(
        windowSize = 12,
        stabilityThreshold = 0.75f,
        confidenceThreshold = 0.80f,
        cooldownMs = 1000L
    )
    
    private var lastInferenceTimestamp: Long = 0

    companion object {
        private const val INFERENCE_INTERVAL_MS = 100L
    }

    private var remoteService: RemoteInferenceService? = null
    private var currentBaseUrl: String? = null
    private val isProcessingRemote = AtomicBoolean(false)

    private fun getRemoteService(): RemoteInferenceService? {
        val url = preferences.aiModelUrl
        if (url.isEmpty()) return null
        val baseUrl = if (url.endsWith("/")) url else "$url/"
        if (baseUrl == currentBaseUrl && remoteService != null) return remoteService

        return try {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder().addInterceptor(logging).build()
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
            null
        }
    }

    /**
     * PRODUCTION-GRADE on-device inference using TFLite and exact Python preprocessing.
     * Throttled to 100ms.
     */
    fun predictOnDevice(result: HandLandmarkerResult) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInferenceTimestamp < INFERENCE_INTERVAL_MS) return
        lastInferenceTimestamp = currentTime

        val features = featureExtractor.extract67Features(result)
        
        // Presence flags: features[0] is Left, features[32] is Right
        val leftHandPresent = features[0] == 1f
        val rightHandPresent = features[32] == 1f

        if (!leftHandPresent && !rightHandPresent) {
            _prediction.value = PredictionResult(
                prediction = "No Hand Detected",
                confidence = 0f,
                status = "PLACE HAND IN FRAME"
            )
            stabilizer.clear()
            return
        }

        val inferenceResult = handModelInference.runInference(features) ?: return

        // Use stabilizer for temporal smoothing
        val stableLabel = stabilizer.addPrediction(inferenceResult.label, inferenceResult.confidence)

        _prediction.value = PredictionResult(
            prediction = stableLabel ?: inferenceResult.label,
            confidence = inferenceResult.confidence,
            isStable = stableLabel != null,
            status = when {
                stableLabel != null -> "STABLE"
                else -> "HOLD STEADY"
            }
        )

        // Extensive Logging
        Log.d("ModelInference", "--- INFERENCE FRAME ---")
        Log.d("ModelInference", "Raw Features (first 5): ${features.take(5).joinToString()}")
        Log.d("ModelInference", "Hands: L=$leftHandPresent, R=$rightHandPresent")
        Log.d("ModelInference", "Prediction: ${inferenceResult.label} (${inferenceResult.confidence})")
        Log.d("ModelInference", "Stable Label: $stableLabel")
        Log.d("ModelInference", "Status: ${_prediction.value?.status}")
    }

    fun predictRemote(result: HandLandmarkerResult) {
        if (isProcessingRemote.get()) return
        val service = getRemoteService() ?: return

        val leftHandPoints = mutableListOf<List<Float>>()
        val rightHandPoints = mutableListOf<List<Float>>()

        result.handedness().forEachIndexed { index, handednessList ->
            val label = handednessList.firstOrNull()?.categoryName() ?: ""
            val landmarks = result.landmarks()[index]
            val points = landmarks.mapIndexed { i, landmark ->
                listOf(i.toFloat(), landmark.x(), landmark.y())
            }
            if (label == "Left") leftHandPoints.addAll(points)
            else if (label == "Right") rightHandPoints.addAll(points)
        }

        if (leftHandPoints.isEmpty() && rightHandPoints.isEmpty()) {
            _prediction.value = PredictionResult("No Hand", 0f, "PLACE HAND IN FRAME")
            return
        }

        val request = SignInferenceRequest(
            landmarks = LandmarkContainer(Left = leftHandPoints, Right = rightHandPoints)
        )

        _isNetworkBusy.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.predictSign(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200 && body.data != null) {
                        val confidence = body.data.confidence
                        val prediction = if (confidence >= 0.7f) body.data.sign else "Unknown"
                        _prediction.value = PredictionResult(
                            prediction = prediction,
                            confidence = confidence,
                            status = if (confidence >= 0.7f) "STABLE" else "LOW CONFIDENCE",
                            isStable = confidence >= 0.7f
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore error for now
            } finally {
                _isNetworkBusy.value = false
                isProcessingRemote.set(false)
            }
        }
    }

    fun clearBuffer() {
        stabilizer.clear()
        _prediction.value = null
    }

    fun canSubmitPrediction(expectedLabel: String): Boolean {
        val current = _prediction.value ?: return false
        return current.isStable && current.prediction.equals(expectedLabel, ignoreCase = true)
    }

    fun markAccepted() {
        clearBuffer()
    }
}
