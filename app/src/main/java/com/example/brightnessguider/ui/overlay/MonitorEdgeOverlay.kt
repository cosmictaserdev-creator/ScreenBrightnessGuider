package com.example.brightnessguider.ui.overlay

import android.graphics.Rect
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.brightnessguider.ui.theme.iOSBlue
import com.example.brightnessguider.ui.theme.GreenNeon

@Composable
fun MonitorEdgeOverlay(
    boundingBox: Rect?,
    imageWidth: Int,
    imageHeight: Int
) {
    if (boundingBox == null || imageWidth <= 0 || imageHeight <= 0) return

    val infiniteTransition = rememberInfiniteTransition(label = "edgeAnim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val scaleX = canvasWidth / imageWidth
        val scaleY = canvasHeight / imageHeight

        val left = boundingBox.left * scaleX
        val top = boundingBox.top * scaleY
        val right = boundingBox.right * scaleX
        val bottom = boundingBox.bottom * scaleY

        val rectSize = Size(right - left, bottom - top)
        val color = GreenNeon.copy(alpha = alpha)

        // Draw Corner Brackets
        val lineLen = 40f
        val strokeWidth = 4f

        // Top-left
        drawLine(color, Offset(left, top), Offset(left + lineLen, top), strokeWidth)
        drawLine(color, Offset(left, top), Offset(left, top + lineLen), strokeWidth)

        // Top-right
        drawLine(color, Offset(right, top), Offset(right - lineLen, top), strokeWidth)
        drawLine(color, Offset(right, top), Offset(right, top + lineLen), strokeWidth)

        // Bottom-left
        drawLine(color, Offset(left, bottom), Offset(left + lineLen, bottom), strokeWidth)
        drawLine(color, Offset(left, bottom), Offset(left, bottom - lineLen), strokeWidth)

        // Bottom-right
        drawLine(color, Offset(right, bottom), Offset(right - lineLen, bottom), strokeWidth)
        drawLine(color, Offset(right, bottom), Offset(right, bottom - lineLen), strokeWidth)
        
        // Faint border
        drawRect(
            color = color.copy(alpha = 0.1f),
            topLeft = Offset(left, top),
            size = rectSize,
            style = Stroke(width = 1f)
        )
        
        // Detection "Graph" or strength indicator
        for (i in 0 until 5) {
            val offset = i * 15f
            drawLine(
                color = color.copy(alpha = 0.2f),
                start = Offset(left - 20f, top + offset),
                end = Offset(left - 10f, top + offset),
                strokeWidth = 2f
            )
        }
    }
}
