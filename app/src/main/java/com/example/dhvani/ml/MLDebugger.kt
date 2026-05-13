package com.example.dhvani.ml

import android.util.Log

object MLDebugger {
    private const val TAG = "MLDebugger"
    private var isEnabled = true

    fun logFeatureVector(features: FloatArray) {
        if (!isEnabled) return
        
        val leftHand = features.sliceArray(0..26)
        val rightHand = features.sliceArray(27..53)
        
        Log.d(TAG, "--- Feature Vector Debug ---")
        Log.d(TAG, "Left Hand Present: ${leftHand[0] == 1f}")
        Log.d(TAG, "Left Hand Gradients: ${leftHand.sliceArray(1..20).joinToString(", ")}")
        Log.d(TAG, "Right Hand Present: ${rightHand[0] == 1f}")
        Log.d(TAG, "Right Hand Gradients: ${rightHand.sliceArray(1..20).joinToString(", ")}")
    }

    fun logTopK(labels: List<String>, confidences: FloatArray, k: Int = 3) {
        if (!isEnabled) return
        
        val topK = confidences.withIndex()
            .sortedByDescending { it.value }
            .take(k)
            .map { labels[it.index] to it.value }
            
        Log.d(TAG, "Top $k Predictions:")
        topK.forEach { (label, conf) ->
            Log.d(TAG, "  $label: ${String.format("%.2f", conf)}")
        }
    }

    fun verifyPreprocessing(input: FloatArray, expected: FloatArray): Boolean {
        if (input.size != expected.size) return false
        var match = true
        for (i in input.indices) {
            if (Math.abs(input[i] - expected[i]) > 1e-4) {
                Log.e(TAG, "Mismatch at index $i: Expected ${expected[i]}, Got ${input[i]}")
                match = false
            }
        }
        return match
    }
}
