package com.example.dhvani.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
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
    private var handLandmarker: HandLandmarker? = null
    
    // We store the dimensions of the frames being sent to MediaPipe
    private var lastImageWidth: Int = 0
    private var lastImageHeight: Int = 0
    private var lastRotation: Int = 0

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .setDelegate(Delegate.CPU) // Use GPU if available in production, CPU for stability in dev
            
            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinHandDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setNumHands(2) // Support for up to 2 hands
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, _ ->
                    // Calculate "Logical" dimensions after rotation
                    // If rotated 90 or 270, width and height are swapped
                    val isRotated = lastRotation == 90 || lastRotation == 270
                    val logicalWidth = if (isRotated) lastImageHeight else lastImageWidth
                    val logicalHeight = if (isRotated) lastImageWidth else lastImageHeight
                    
                    resultListener?.onResults(result, logicalHeight, logicalWidth)
                }
                .setErrorListener { error ->
                    resultListener?.onError(error.message ?: "Unknown MediaPipe error")
                }

            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            resultListener?.onError("Hand Landmarker failed to initialize: " + e.message)
            Log.e("HandLandmarkerHelper", "Initialization failed", e)
        } catch (e: Error) {
            resultListener?.onError("Hand Landmarker native error: " + e.message)
            Log.e("HandLandmarkerHelper", "Native error", e)
        }
    }

    fun detectLiveStream(
        imageProxy: androidx.camera.core.ImageProxy
    ) {
        if (handLandmarker == null) {
            imageProxy.close()
            return
        }

        try {
            val frameTime = System.currentTimeMillis()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            
            // Store frame info for coordinate mapping in results
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
        fun onResults(result: HandLandmarkerResult, imageHeight: Int, imageWidth: Int)
    }
}
