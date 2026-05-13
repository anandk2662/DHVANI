package com.example.dhvani.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HandModelInference(context: Context) {

    companion object {
        // Complete relative path within the assets folder
        private const val MODEL_ASSET_PATH = "models/hand_model.tflite"
        
        // 35 labels matching the model's output shape [1, 35]
        private val LABELS = listOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        )
    }

    private var interpreter: Interpreter? = null

    init {
        try {
            val modelBuffer = loadModelFile(context, MODEL_ASSET_PATH)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            android.util.Log.e("HandModelInference", "Failed to load TFLite model: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Runs inference on the given 54-dimensional feature vector.
     * Returns a Pair of (Predicted Label, Confidence).
     */
    fun runInference(features: FloatArray): Pair<String, Float> {
        val currentInterpreter = interpreter ?: return "Error" to 0f

        // Input shape: [1, 54]
        val input = arrayOf(features)
        
        // Output shape: [1, 35]
        val output = Array(1) { FloatArray(LABELS.size) }

        try {
            currentInterpreter.run(input, output)
        } catch (e: Exception) {
            android.util.Log.e("HandModelInference", "Inference failed: ${e.message}")
            return "Error" to 0f
        }

        // Find the index with the highest confidence
        var maxIdx = -1
        var maxConf = -1f

        for (i in LABELS.indices) {
            if (output[0][i] > maxConf) {
                maxConf = output[0][i]
                maxIdx = i
            }
        }

        return if (maxIdx != -1) {
            LABELS[maxIdx] to maxConf
        } else {
            "Unknown" to 0f
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
