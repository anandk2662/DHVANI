package com.example.dhvani.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class SignLandmark(
    val x: Float,
    val y: Float,
    val z: Float
)

@Serializable
data class HandData(
    val landmarks: List<SignLandmark>,
    val flattenedVector: FloatArray, // 63 features (21 points * 3)
    val handedness: String,
    val score: Float
)

class LandmarkProcessor {
    
    /**
     * Extracts landmarks, normalizes them relative to the wrist (Point 0),
     * and flattens them into a 1D feature vector.
     */
    fun process(result: HandLandmarkerResult): List<HandData> {
        val allHandData = mutableListOf<HandData>()
        
        result.landmarks().forEachIndexed { index, landmarks ->
            val handedness = result.handedness().getOrNull(index)?.getOrNull(0)
            
            // 1. Convert to custom Landmark list
            val rawLandmarks = landmarks.map { 
                SignLandmark(it.x(), it.y(), it.z())
            }
            
            // 2. Normalize relative to wrist (Wrist is always at index 0)
            val wrist = rawLandmarks[0]
            val normalizedLandmarks = rawLandmarks.map { 
                SignLandmark(
                    x = it.x - wrist.x,
                    y = it.y - wrist.y,
                    z = it.z - wrist.z
                )
            }
            
            // 3. Flatten into feature vector (63 floats)
            val flattened = FloatArray(63)
            normalizedLandmarks.forEachIndexed { i, lm ->
                flattened[i * 3] = lm.x
                flattened[i * 3 + 1] = lm.y
                flattened[i * 3 + 2] = lm.z
            }
            
            allHandData.add(
                HandData(
                    landmarks = normalizedLandmarks, // Using normalized for internal tracking
                    flattenedVector = flattened,
                    handedness = handedness?.categoryName() ?: "Unknown",
                    score = handedness?.score() ?: 0f
                )
            )
        }
        
        return allHandData
    }

    fun toJson(handData: HandData): String {
        return Json.encodeToString(handData.landmarks)
    }
}
