package com.example.dhvani.ml

import android.util.Log

/**
 * Handles temporal smoothing and prediction stability logic.
 */
class PredictionStabilizer(
    private val windowSize: Int = 12,
    private val stabilityThreshold: Float = 0.75f,
    private val confidenceThreshold: Float = 0.80f,
    private val cooldownMs: Long = 1000L
) {
    private val window = mutableListOf<Prediction>()
    private var lastAcceptedTimestamp: Long = 0

    data class Prediction(val label: String, val confidence: Float)

    /**
     * Adds a new prediction and returns the stabilized label if thresholds are met.
     */
    fun addPrediction(label: String, confidence: Float): String? {
        val currentTime = System.currentTimeMillis()
        
        // 1. Add to rolling window
        window.add(Prediction(label, confidence))
        if (window.size > windowSize) {
            window.removeAt(0)
        }

        if (window.size < windowSize) return null

        // 2. Majority Voting
        val counts = window.groupingBy { it.label }.eachCount()
        val dominantLabel = counts.maxByOrNull { it.value }?.key ?: return null
        val stability = counts[dominantLabel]!!.toFloat() / windowSize

        // 3. Confidence Averaging
        val avgConfidence = window.filter { it.label == dominantLabel }
            .map { it.confidence }
            .average()
            .toFloat()

        Log.d("Stabilizer", "Dominant: $dominantLabel, Stability: $stability, AvgConf: $avgConfidence")

        // 4. Threshold Checks
        if (stability >= stabilityThreshold && avgConfidence >= confidenceThreshold) {
            // 5. Cooldown Check
            if (currentTime - lastAcceptedTimestamp > cooldownMs) {
                lastAcceptedTimestamp = currentTime
                Log.i("Stabilizer", "✅ STABLE PREDICTION: $dominantLabel")
                return dominantLabel
            } else {
                Log.d("Stabilizer", "Prediction stable but in cooldown")
            }
        }

        return null
    }

    fun clear() {
        window.clear()
    }
}
