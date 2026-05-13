package com.example.dhvani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RoadmapPath(isRightToLeft: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height

        val startX = if (isRightToLeft) width * 0.75f else width * 0.25f
        val endX = if (isRightToLeft) width * 0.25f else width * 0.75f

        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, height * 0.5f,
                endX, height * 0.5f,
                endX, height
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray.copy(alpha = 0.5f),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}