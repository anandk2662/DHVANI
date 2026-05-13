package com.example.dhvani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.dhvani.ui.theme.PrimaryGreen

@Composable
fun RoadmapPath(
    isRightToLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val width = size.width
        val height = size.height
        val path = Path()

        if (isRightToLeft) {
            // Path from Right to Left
            path.moveTo(width - 100f, 0f)
            path.cubicTo(
                width - 100f, height / 2,
                100f, height / 2,
                100f, height
            )
        } else {
            // Path from Left to Right
            path.moveTo(100f, 0f)
            path.cubicTo(
                100f, height / 2,
                width - 100f, height / 2,
                width - 100f, height
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray.copy(alpha = 0.3f),
            style = Stroke(
                width = 8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
            )
        )
    }
}
