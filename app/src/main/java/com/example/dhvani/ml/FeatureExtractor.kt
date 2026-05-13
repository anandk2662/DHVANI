package com.example.dhvani.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.*

class FeatureExtractor {

    companion object {
        private const val PI = Math.PI.toFloat()

        private val GRADIENT_PAIRS = listOf(
            4 to 3, 3 to 2, 2 to 1, 1 to 0,
            8 to 7, 7 to 6, 6 to 5, 5 to 0,
            12 to 11, 11 to 10, 10 to 9, 9 to 0,
            16 to 15, 15 to 14, 14 to 13, 13 to 0,
            20 to 19, 19 to 18, 18 to 17, 17 to 0
        )

        private val DOT_TRIPLETS = listOf(
            Triple(0, 5, 17),
            Triple(4, 0, 8),
            Triple(8, 0, 12),
            Triple(12, 0, 16),
            Triple(16, 0, 20)
        )

        private val CROSS_TRIPLET = Triple(5, 0, 17)
    }

    /**
     * Replicates the Python lmFeatures function.
     * Takes a list of 21 landmarks (x, y) and returns 26 features.
     */
    private fun extractHandFeatures(landmarks: List<Pair<Float, Float>>): FloatArray {
        val features = mutableListOf<Float>()

        // 1. Gradient features (20)
        for ((idx1, idx2) in GRADIENT_PAIRS) {
            val (x1, y1) = landmarks[idx1]
            val (x2, y2) = landmarks[idx2]

            if (x1 == 0f && y1 == 0f && x2 == 0f && y2 == 0f) {
                features.add(0f)
            } else {
                val angle = (atan2(y2 - y1, x2 - x1) + PI) / (2 * PI)
                features.add(angle)
            }
        }

        // 2. Dot features (5)
        for ((idx1, idx2, idx3) in DOT_TRIPLETS) {
            val (x1, y1) = landmarks[idx1]
            val (x2, y2) = landmarks[idx2]
            val (x3, y3) = landmarks[idx3]

            val apX = x1 - x2
            val apY = y1 - y2
            val aqX = x3 - x2
            val aqY = y3 - y2

            val dot = apX * aqX + apY * aqY
            val magsSq = (apX * apX + apY * apY) * (aqX * aqX + aqY * aqY)
            val mags = sqrt(magsSq)

            if (mags == 0f) {
                features.add(0f)
            } else {
                // Ensure the value is within [-1, 1] for acos
                val cosTheta = (dot / mags).coerceIn(-1f, 1f)
                val angle = acos(cosTheta) / PI
                features.add(angle)
            }
        }

        // 3. Cross feature (1)
        val (idx1, idx0, idx2) = CROSS_TRIPLET
        val (x1, y1) = landmarks[idx1]
        val (x0, y0) = landmarks[idx0]
        val (x2, y2) = landmarks[idx2]

        val aX = x1 - x0
        val aY = y1 - y0
        val bX = x2 - x0
        val bY = y2 - y0

        val crossProduct = aX * bY - aY * bX
        val magsSq = (aX * aX + aY * aY) * (bX * bX + bY * bY)
        val mags = sqrt(magsSq)

        val sinTheta = if (mags == 0f) 0f else crossProduct / mags
        features.add((sinTheta + 1) / 2f)

        return features.toFloatArray()
    }

    /**
     * Extracts features from MediaPipe HandLandmarkerResult.
     * Normalizes both hands into a single 54-dimensional feature vector.
     */
    fun extractFeatures(result: HandLandmarkerResult): FloatArray {
        val finalFeatures = FloatArray(54) { 0f }

        // Initialize with zeros (padding)
        var leftHandLms: List<Pair<Float, Float>>? = null
        var rightHandLms: List<Pair<Float, Float>>? = null

        // MediaPipe result contains multiple hands. We need to identify Left vs Right.
        // handednesses is a list of lists of Handedness.
        result.handedness().forEachIndexed { index, handednessList ->
            val label = handednessList.firstOrNull()?.categoryName() ?: ""
            val landmarks = result.landmarks()[index].map { it.x() to it.y() }
            
            if (label == "Left") {
                leftHandLms = landmarks
            } else if (label == "Right") {
                rightHandLms = landmarks
            }
        }

        // Process Left Hand (Index 0 to 26 in final array)
        leftHandLms?.let { landmarks ->
            if (landmarks.size == 21) {
                finalFeatures[0] = 1f // Presence flag
                val handFeats = extractHandFeatures(landmarks)
                handFeats.copyInto(finalFeatures, 1)
            }
        } ?: run {
            finalFeatures[0] = 0f // Presence flag
        }

        // Process Right Hand (Index 27 to 53 in final array)
        rightHandLms?.let { landmarks ->
            if (landmarks.size == 21) {
                finalFeatures[27] = 1f // Presence flag
                val handFeats = extractHandFeatures(landmarks)
                handFeats.copyInto(finalFeatures, 28)
            }
        } ?: run {
            finalFeatures[27] = 0f // Presence flag
        }

        return finalFeatures
    }
}
