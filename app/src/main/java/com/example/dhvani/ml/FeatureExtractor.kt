package com.example.dhvani.ml

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.*

/**
 * PRODUCTION-GRADE Feature Extractor
 * EXACTLY matches Python training preprocessing for 67-dimensional feature vector.
 */
class FeatureExtractor {

    companion object {
        private const val PI_F = Math.PI.toFloat()
        private const val TWO_PI_F = (2.0 * Math.PI).toFloat()

        private val GRADIENT_PAIRS = listOf(
            4 to 3, 3 to 2, 2 to 1, 1 to 0,
            8 to 7, 7 to 6, 6 to 5, 5 to 0,
            12 to 11, 11 to 10, 10 to 9, 9 to 0,
            16 to 15, 15 to 14, 14 to 13, 13 to 0,
            20 to 19, 19 to 18, 18 to 17, 17 to 0
        )

        private val DOT_TRIPLETS = listOf(
            Triple(0, 5, 17), Triple(4, 0, 8), Triple(8, 0, 12), Triple(12, 0, 16), Triple(16, 0, 20)
        )

        private val CROSS_TRIPLET = Triple(5, 0, 17)
        private val FINGER_TIPS = listOf(4, 8, 12, 16, 20)
    }

    fun extract67Features(result: HandLandmarkerResult): FloatArray {
        val features = FloatArray(67) { 0f }
        
        val (leftHand, rightHand) = sortHandsConsistently(result)

        // 1. Fill Left Hand Features (Index 0 to 31)
        if (leftHand != null) {
            features[0] = 1f // Presence flag
            extractSingleHandFeatures(leftHand).copyInto(features, 1)
        }

        // 2. Fill Right Hand Features (Index 32 to 63)
        if (rightHand != null) {
            features[32] = 1f // Presence flag
            extractSingleHandFeatures(rightHand).copyInto(features, 33)
        }

        // 3. Fill Inter-hand Features (Index 64 to 66)
        if (leftHand != null && rightHand != null) {
            val lWrist = leftHand[0]
            val rWrist = rightHand[0]
            
            val lScale = dist(leftHand[0], leftHand[9])
            val rScale = dist(rightHand[0], rightHand[9])
            val avgScale = (lScale + rScale) / 2f
            
            // Feature 64: Wrist-to-wrist distance normalized by scale
            features[64] = dist(lWrist, rWrist) / (avgScale.coerceAtLeast(0.001f))
            
            // Feature 65: Relative angle
            val angle = (atan2(rWrist.y() - lWrist.y(), rWrist.x() - lWrist.x()) + PI_F) / TWO_PI_F
            features[65] = angle.coerceIn(0f, 1f)
            
            // Feature 66: Average scale
            features[66] = avgScale
        }

        return features
    }

    private fun sortHandsConsistently(result: HandLandmarkerResult): Pair<List<NormalizedLandmark>?, List<NormalizedLandmark>?> {
        val hands = mutableListOf<HandData>()
        result.landmarks().forEachIndexed { index, landmarks ->
            val label = result.handedness()[index].firstOrNull()?.categoryName() ?: "Unknown"
            hands.add(HandData(label, landmarks))
        }

        if (hands.isEmpty()) return null to null
        
        if (hands.size == 1) {
            val hand = hands[0]
            // For single hand, trust label but fallback to X if unknown
            return when (hand.label) {
                "Left" -> hand.landmarks to null
                "Right" -> null to hand.landmarks
                else -> {
                    if (hand.landmarks[0].x() < 0.5f) hand.landmarks to null
                    else null to hand.landmarks
                }
            }
        }

        // 2 or more hands: Always sort by X-position for consistency
        // The hand with smaller X is "Left" in image space
        val sorted = hands.sortedBy { it.landmarks[0].x() }
        return sorted[0].landmarks to sorted[1].landmarks
    }

    private fun extractSingleHandFeatures(lm: List<NormalizedLandmark>): FloatArray {
        val f = mutableListOf<Float>()

        // 1. Gradient Features (20)
        GRADIENT_PAIRS.forEach { (i2, i1) ->
            val dy = lm[i2].y() - lm[i1].y()
            val dx = lm[i2].x() - lm[i1].x()
            val angle = (atan2(dy, dx) + PI_F) / TWO_PI_F
            val quantized = round(angle * 20f) / 20f
            f.add(quantized.coerceIn(0f, 1f))
        }

        // 2. Dot Features (5)
        DOT_TRIPLETS.forEach { (a, b, c) ->
            val v1x = lm[a].x() - lm[b].x()
            val v1y = lm[a].y() - lm[b].y()
            val v2x = lm[c].x() - lm[b].x()
            val v2y = lm[c].y() - lm[b].y()
            
            val dot = v1x * v2x + v1y * v2y
            val mag1 = sqrt(v1x * v1x + v1y * v1y).coerceAtLeast(0.0001f)
            val mag2 = sqrt(v2x * v2x + v2y * v2y).coerceAtLeast(0.0001f)
            
            val value = acos((dot / (mag1 * mag2)).coerceIn(-1f, 1f)) / PI_F
            f.add(round(value * 10f) / 10f)
        }

        // 3. Cross Feature (1)
        val (ca, cb, cc) = CROSS_TRIPLET
        val v1x = lm[ca].x() - lm[cb].x()
        val v1y = lm[ca].y() - lm[cb].y()
        val v2x = lm[cc].x() - lm[cb].x()
        val v2y = lm[cc].y() - lm[cb].y()
        
        val cross = v1x * v2y - v1y * v2x
        val mags = (sqrt(v1x * v1x + v1y * v1y) * sqrt(v2x * v2x + v2y * v2y)).coerceAtLeast(0.0001f)
        f.add(((cross / mags) + 1f) / 2f)

        // 4. Binary Closure (5)
        val wrist = lm[0]
        val knuckle = lm[9]
        val palmRadius = dist(wrist, knuckle)
        FINGER_TIPS.forEach { tipIdx ->
            val tipDist = dist(wrist, lm[tipIdx])
            f.add(if (tipDist < palmRadius) 1f else 0f)
        }

        return f.toFloatArray()
    }

    private fun dist(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        return sqrt((p1.x() - p2.x()).pow(2) + (p1.y() - p2.y()).pow(2))
    }

    private data class HandData(val label: String, val landmarks: List<NormalizedLandmark>)
}
