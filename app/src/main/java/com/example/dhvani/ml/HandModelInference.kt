package com.example.dhvani.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/**
 * PRODUCTION-GRADE TFLite Inference Wrapper
 */
class HandModelInference(private val context: Context) {

    companion object {
        private const val INFERENCE_INTERVAL_MS = 100L
    }

    private var interpreter: Interpreter? = null
    private val labelMapper = LabelMapper(context)
    private var lastInferenceTime: Long = 0

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(true)
            }
            interpreter = Interpreter(loadModelFile(), options)
            Log.i("HandModelInference", "TFLite Interpreter initialized successfully")
        } catch (e: Exception) {
            Log.e("HandModelInference", "Error initializing TFLite", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("models/hand_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun runInference(features: FloatArray): InferenceResult? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInferenceTime < INFERENCE_INTERVAL_MS) {
            return null
        }
        lastInferenceTime = currentTime

        if (interpreter == null) return null

        // 1. Prepare Input [1, 67]
        val input = arrayOf(features)
        
        // 2. Prepare Output [1, 35]
        val outputCount = labelMapper.getAllLabels().size
        val output = Array(1) { FloatArray(outputCount) }

        // 3. Log Raw Data for Debugging
        Log.d("Inference", "Input Tensor Shape: [1, ${features.size}]")
        // Log.v("Inference", "Raw Features: ${features.joinToString(", ")}")

        // 4. Run Model
        interpreter?.run(input, output)

        // 5. Process Results
        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        
        if (maxIndex == -1) return null

        val label = labelMapper.getLabel(maxIndex)
        val confidence = probabilities[maxIndex]

        // 6. Top-3 Debugging
        val top3 = probabilities.withIndex()
            .sortedByDescending { it.value }
            .take(3)
            .joinToString { "${labelMapper.getLabel(it.index)} (${String.format(Locale.US, "%.2f", it.value)})" }
        
        Log.d("Inference", "Prediction: $label ($confidence), Top-3: $top3")

        return InferenceResult(label, confidence, probabilities.toList())
    }

    data class InferenceResult(
        val label: String,
        val confidence: Float,
        val allProbabilities: List<Float>
    )

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
