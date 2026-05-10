package com.example.dhvani.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var boxPaint = Paint()

    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var isFrontCamera: Boolean = false

    init {
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.CYAN
        linePaint.strokeWidth = 8f
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 12f
        pointPaint.style = Paint.Style.FILL

        boxPaint.color = Color.GREEN
        boxPaint.strokeWidth = 4f
        boxPaint.style = Paint.Style.STROKE
    }

    fun setResults(
        handLandmarkerResult: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        isFrontCamera: Boolean
    ) {
        results = handLandmarkerResult
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.isFrontCamera = isFrontCamera

        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { handLandmarkerResult ->
            if (imageWidth <= 0 || imageHeight <= 0) return

            // Calculate scale and offsets for FILL_CENTER alignment
            // This logic maps normalized [0, 1] coordinates from the "upright" AI frame
            // to the actual pixels on the screen, accounting for the crop used by CameraX PreviewView.
            val scale = Math.max(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
            val offsetX = (width - imageWidth * scale) / 2f
            val offsetY = (height - imageHeight * scale) / 2f

            for (landmarks in handLandmarkerResult.landmarks()) {
                // Draw Hand Skeleton
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    val start = landmarks.get(it.start())
                    val end = landmarks.get(it.end())
                    
                    var startX = start.x() * imageWidth * scale + offsetX
                    val startY = start.y() * imageHeight * scale + offsetY
                    var endX = end.x() * imageWidth * scale + offsetX
                    val endY = end.y() * imageHeight * scale + offsetY
                    
                    if (isFrontCamera) {
                        startX = width - startX
                        endX = width - endX
                    }

                    canvas.drawLine(startX, startY, endX, endY, linePaint)
                }

                // Draw Landmarks
                for (landmark in landmarks) {
                    var x = landmark.x() * imageWidth * scale + offsetX
                    val y = landmark.y() * imageHeight * scale + offsetY
                    
                    if (isFrontCamera) {
                        x = width - x
                    }

                    canvas.drawPoint(x, y, pointPaint)
                }

                // Draw Bounding Box (Debug)
                drawBoundingBox(canvas, landmarks, scale, offsetX, offsetY)
            }
        }
    }

    private fun drawBoundingBox(canvas: Canvas, landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>, scale: Float, offsetX: Float, offsetY: Float) {
        var minX = 1f
        var maxX = 0f
        var minY = 1f
        var maxY = 0f

        for (landmark in landmarks) {
            minX = Math.min(minX, landmark.x())
            maxX = Math.max(maxX, landmark.x())
            minY = Math.min(minY, landmark.y())
            maxY = Math.max(maxY, landmark.y())
        }

        var left = minX * imageWidth * scale + offsetX
        var right = maxX * imageWidth * scale + offsetX
        val top = minY * imageHeight * scale + offsetY
        val bottom = maxY * imageHeight * scale + offsetY

        if (isFrontCamera) {
            val tempLeft = width - right
            right = width - left
            left = tempLeft
        }

        canvas.drawRect(left, top, right, bottom, boxPaint)
    }
}
