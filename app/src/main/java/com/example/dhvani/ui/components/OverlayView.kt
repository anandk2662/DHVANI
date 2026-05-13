package com.example.dhvani.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var rotationDegrees: Int = 0
    private var isFrontCamera: Boolean = false
    
    init {
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.CYAN
        linePaint.strokeWidth = 8f
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = 12f
        pointPaint.style = Paint.Style.FILL
    }

    fun setResults(
        handLandmarkerResult: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        rotationDegrees: Int,
        isFrontCamera: Boolean
    ) {
        results = handLandmarkerResult
        this.imageHeight = imageHeight // Sensor Height
        this.imageWidth = imageWidth   // Sensor Width
        this.rotationDegrees = rotationDegrees
        this.isFrontCamera = isFrontCamera
        
        invalidate()
    }

    // Helper for 3D math and coordinate conversion
    data class Vec3(val x: Float, val y: Float, val z: Float) {
        operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
        fun normalize(): Vec3 {
            val len = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            return if (len > 0) Vec3(x / len, y / len, z / len) else this
        }
        fun cross(other: Vec3) = Vec3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    private fun com.google.mediapipe.tasks.components.containers.NormalizedLandmark.toVec3(): Vec3 {
        // 1. Raw normalized coordinates from MediaPipe (relative to sensor)
        var nx = x()
        var ny = y()
        
        // 3. Remap landmarks based on rotationDegrees to match screen orientation
        when (rotationDegrees) {
            90 -> {
                val temp = nx
                nx = 1.0f - ny
                ny = temp
            }
            180 -> {
                nx = 1.0f - nx
                ny = 1.0f - ny
            }
            270 -> {
                val temp = nx
                nx = ny
                ny = 1.0f - temp
            }
        }
        
        // 4. Mirroring (Applied for front camera)
        if (isFrontCamera) {
            nx = 1.0f - nx
        }
        
        // 5. Convert to Camera-Local coordinates
        var vx = nx - 0.5f
        var vy = -(ny - 0.5f)
        var vz = -z()
        
        // 6. Apply aspect ratio
        val aspect = width.toFloat() / height.toFloat()
        vx *= aspect
        
        return Vec3(vx, vy, vz)
    }

    private fun projectVec3(v: Vec3, scale: Float): PointF {
        val aspect = width.toFloat() / height.toFloat()
        // Convert back to normalized [0, 1] logical space
        val nx = (v.x / aspect) + 0.5f
        val ny = 0.5f - v.y
        
        // Upright logical dimensions based on rotation
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val lw = if (isRotated) imageHeight else imageWidth
        val lh = if (isRotated) imageWidth else imageHeight
        
        // Scale to logical pixels and center in View (FILL_CENTER)
        val px = (nx - 0.5f) * lw * scale + width / 2f
        val py = (ny - 0.5f) * lh * scale + height / 2f
        
        return PointF(px, py)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { handLandmarkerResult ->
            // Logical dimensions of the upright image
            val isRotated = rotationDegrees == 90 || rotationDegrees == 270
            val lw = if (isRotated) imageHeight else imageWidth
            val lh = if (isRotated) imageWidth else imageHeight
            
            // 8. Calculate FILL_CENTER scaling to the View's dimensions
            val scale = Math.max(width.toFloat() / lw, height.toFloat() / lh)
            
            for (landmarks in handLandmarkerResult.landmarks()) {
                
                // 9. Rebuild hand orientation basis from landmarks
                val wristV = landmarks[0].toVec3()
                val indexMCPV = landmarks[5].toVec3()
                val middleMCPV = landmarks[9].toVec3()
                val pinkyMCPV = landmarks[17].toVec3()
                
                val right = (indexMCPV - pinkyMCPV).normalize()
                val up = (middleMCPV - wristV).normalize()
                val forward = right.cross(up).normalize()
                
                // Draw Connections
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    val startV = landmarks[it.start()].toVec3()
                    val endV = landmarks[it.end()].toVec3()
                    
                    val p1 = projectVec3(startV, scale)
                    val p2 = projectVec3(endV, scale)
                    
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                }

                // 10. Draw Landmarks (Wrist at 0 is the anchor)
                for (landmark in landmarks) {
                    val v = landmark.toVec3()
                    val p = projectVec3(v, scale)
                    canvas.drawPoint(p.x, p.y, pointPaint)
                }
            }
        }
    }
}
