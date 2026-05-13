package com.example.dhvani.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandModelInference @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG = "HandModelInference"
        private const val MODEL_ASSET_PATH = "models/hand_model.tflite"
        private const val LABELS_ASSET_PATH = "models/labels.json"
    }

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    init {
        setupInterpreter()
        loadLabels()
    }

    private fun setupInterpreter() {
        try {
            val modelBuffer = loadModelFile(context, MODEL_ASSET_PATH)
            val options = Interpreter.Options().apply {
                // Optimization: Use GPU if available, else 4 threads on CPU
                val compatList = CompatibilityList()
                if (compatList.isDelegateSupportedOnThisDevice) {
                    val delegateOptions = compatList.bestOptionsForThisDevice
                    addDelegate(GpuDelegate(delegateOptions))
                } else {
                    setNumThreads(4)
                }
                setUseNNAPI(true)
            }
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "TFLite Interpreter initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite model: ${e.message}")
        }
    }

    private fun loadLabels() {
        try {
            val jsonString = context.assets.open(LABELS_ASSET_PATH).bufferedReader().use { it.readText() }
            // Using org.json for more robust parsing of simple string arrays
            val jsonArray = org.json.JSONArray(jsonString)
            val tempLabels = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                tempLabels.add(jsonArray.getString(i))
            }
            labels = tempLabels
            Log.d(TAG, "Successfully loaded ${labels.size} labels from $LABELS_ASSET_PATH: $labels")
        } catch (e: Exception) {
            Log.e(TAG, "Critical Error: Failed to load labels.json from assets/models/labels.json. Using generic fallback.", e)
            // Fallback to ensure the app doesn't crash, though predictions will be wrong
            labels = (0..34).map { "Class ${it + 1}" }
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
     * Implementation follows the provided Python logic:
     * 1. Reshape to [1, 54]
     * 2. Invoke Interpreter
     * 3. Argmax the prediction probabilities
     * 4. Map index to character using alphabet_map
     */
    fun runInference(features: FloatArray): Pair<String, Float> {
        val currentInterpreter = interpreter ?: return "Error: Model not loaded" to 0f

        // 1. Input preparation: [1, 54]
        val input = arrayOf(features)
        
        // 2. Output preparation: Detect size from model to avoid mismatch
        val outputTensor = currentInterpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape() // [1, NUM_CLASSES]
        val numClasses = outputShape[1]
        val output = Array(1) { FloatArray(numClasses) }

        try {
            // 3. Invoke interpreter
            currentInterpreter.run(input, output)
            
            // 4. Argmax and mapping (Python: np.argmax(prediction))
            var maxIdx = 0
            var maxConf = output[0][0]

            for (i in 1 until numClasses) {
                if (output[0][i] > maxConf) {
                    maxConf = output[0][i]
                    maxIdx = i
                }
            }

            // Map index to label (Python: alphabet_map[predicted_index])
            val label = labels.getOrElse(maxIdx) { "Unknown Index: $maxIdx" }
            return label to maxConf

        } catch (e: Exception) {
            Log.e(TAG, "Inference failed: ${e.message}")
            return "Inference Error" to 0f
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

