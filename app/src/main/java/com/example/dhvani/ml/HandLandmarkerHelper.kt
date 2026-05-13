package com.example.dhvani.ml

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    val context: Context,
    val resultListener: LandmarkerListener? = null
) {
    companion object {
        private const val MODEL_ASSET_PATH = "hand_landmarker.task"
    }

    private var handLandmarker: HandLandmarker? = null
    
    private var lastImageWidth: Int = 0
    private var lastImageHeight: Int = 0
    private var lastRotation: Int = 0

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_PATH)
                .setDelegate(Delegate.CPU)
            
            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinHandDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setNumHands(2)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, _ ->
                    resultListener?.onResults(
                        result, 
                        lastImageHeight, 
                        lastImageWidth, 
                        lastRotation
                    )
                }
                .setErrorListener { error ->
                    resultListener?.onError(error.message ?: "Unknown MediaPipe error")
                }

            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            resultListener?.onError("Hand Landmarker failed to initialize: " + e.message)
            Log.e("HandLandmarkerHelper", "Initialization failed", e)
        }
    }

    fun detectLiveStream(imageProxy: androidx.camera.core.ImageProxy) {
        if (handLandmarker == null) {
            imageProxy.close()
            return
        }

        try {
            val frameTime = System.currentTimeMillis()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            
            lastImageWidth = imageProxy.width
            lastImageHeight = imageProxy.height
            lastRotation = rotationDegrees

            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            val imageProcessingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(rotationDegrees)
                .build()

            handLandmarker?.detectAsync(mpImage, imageProcessingOptions, frameTime)
        } catch (e: Exception) {
            Log.e("HandLandmarkerHelper", "Detection failed", e)
        } finally {
            imageProxy.close()
        }
    }

    interface LandmarkerListener {
        fun onError(error: String)
        fun onResults(result: HandLandmarkerResult, imageHeight: Int, imageWidth: Int, rotationDegrees: Int)
    }
}
