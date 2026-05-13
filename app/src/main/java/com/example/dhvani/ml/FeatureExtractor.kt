package com.example.dhvani.ml

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.*

class FeatureExtractor {

    companion object {
        private const val PI = Math.PI.toFloat()

        // Exact gradient pairs from Python implementation
        private val GRADIENT_PAIRS = listOf(
            4 to 3, 3 to 2, 2 to 1, 1 to 0,
            8 to 7, 7 to 6, 6 to 5, 5 to 0,
            12 to 11, 11 to 10, 10 to 9, 9 to 0,
            16 to 15, 15 to 14, 14 to 13, 13 to 0,
            20 to 19, 19 to 18, 18 to 17, 17 to 0
        )

        // Exact dot product triplets (angle at idx2)
        private val DOT_TRIPLETS = listOf(
            Triple(0, 5, 17),
            Triple(4, 0, 8),
            Triple(8, 0, 12),
            Triple(12, 0, 16),
            Triple(16, 0, 20)
        )

        // Exact cross product triplet
        private val CROSS_TRIPLET = Triple(5, 0, 17)
    }

    /**
     * Exact reproduction of Python lmFeatures()
     */
    private fun extractHandFeatures(landmarks: List<Landmark>): FloatArray {
        val features = mutableListOf<Float>()

        // 1. Gradient features (20)
        // angle = (atan2(y2-y1, x2-x1) + PI) / (2*PI)
        for ((idx1, idx2) in GRADIENT_PAIRS) {
            val p1 = landmarks[idx1]
            val p2 = landmarks[idx2]
            
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            
            val angle = (atan2(dy, dx) + PI) / (2 * PI)
            features.add(angle)
        }

        // 2. Dot product angle features (5)
        // acos(dot/magnitude) / PI
        for ((idx1, idx2, idx3) in DOT_TRIPLETS) {
            val p1 = landmarks[idx1]
            val p2 = landmarks[idx2]
            val p3 = landmarks[idx3]

            val v1x = p1.x - p2.x
            val v1y = p1.y - p2.y
            val v2x = p3.x - p2.x
            val v2y = p3.y - p2.y

            val dot = v1x * v2x + v1y * v2y
            val mag1 = sqrt(v1x * v1x + v1y * v1y)
            val mag2 = sqrt(v2x * v2x + v2y * v2y)
            val mags = mag1 * mag2

            if (mags == 0f) {
                features.add(0f)
            } else {
                val cosTheta = (dot / mags).coerceIn(-1f, 1f)
                features.add(acos(cosTheta) / PI)
            }
        }

        // 3. Cross product orientation features (1)
        // ((cross/magnitude)+1)/2
        val (p1Idx, p0Idx, p2Idx) = CROSS_TRIPLET
        val p1 = landmarks[p1Idx]
        val p0 = landmarks[p0Idx]
        val p2 = landmarks[p2Idx]

        val ax = p1.x - p0.x
        val ay = p1.y - p0.y
        val bx = p2.x - p0.x
        val by = p2.y - p0.y

        val cross = ax * by - ay * bx
        val magA = sqrt(ax * ax + ay * ay)
        val magB = sqrt(bx * bx + by * by)
        val mags = magA * magB

        if (mags == 0f) {
            features.add(0.5f) // Neutral orientation
        } else {
            val sinTheta = (cross / mags).coerceIn(-1f, 1f)
            features.add((sinTheta + 1) / 2f)
        }

        return features.toFloatArray()
    }

    private data class Landmark(val x: Float, val y: Float)

    /**
     * Extracts features from MediaPipe Result.
     * Ensures 54-dimensional vector: [LeftHandPresence, 26xLeftFeatures, RightHandPresence, 26xRightFeatures]
     */
    fun extractFeatures(result: HandLandmarkerResult): FloatArray {
        val finalVector = FloatArray(54)

        var leftHand: List<Landmark>? = null
        var rightHand: List<Landmark>? = null

        // MediaPipe Result Handedness Handling
        result.handedness().forEachIndexed { index, handednessList ->
            val label = handednessList.firstOrNull()?.categoryName() ?: ""
            val lms = result.landmarks()[index].map { Landmark(it.x(), it.y()) }
            
            // Note: MediaPipe "Left" is usually the left hand in the image.
            // Depending on camera mirroring, you might need to swap these.
            if (label == "Left") {
                leftHand = lms
            } else if (label == "Right") {
                rightHand = lms
            }
        }

        // Left Hand (indices 0 to 26)
        leftHand?.let { lms ->
            if (lms.size == 21) {
                finalVector[0] = 1f
                val feats = extractHandFeatures(lms)
                feats.copyInto(finalVector, 1)
            }
        } ?: run {
            finalVector[0] = 0f
            val paddingLms = List(21) { i -> Landmark(i.toFloat(), 0f) }
            val feats = extractHandFeatures(paddingLms)
            feats.copyInto(finalVector, 1)
        }

        // Right Hand (indices 27 to 53)
        rightHand?.let { lms ->
            if (lms.size == 21) {
                finalVector[27] = 1f
                val feats = extractHandFeatures(lms)
                feats.copyInto(finalVector, 28)
            }
        } ?: run {
            finalVector[27] = 0f
            val paddingLms = List(21) { i -> Landmark(i.toFloat(), 0f) }
            val feats = extractHandFeatures(paddingLms)
            feats.copyInto(finalVector, 28)
        }


        return finalVector
    }
}

