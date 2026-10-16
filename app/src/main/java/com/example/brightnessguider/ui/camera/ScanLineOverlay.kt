package com.example.brightnessguider.ui.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.brightnessguider.ui.theme.iOSBlue

@Composable
fun ScanLineOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "yOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        val currentY = yOffset * height

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    iOSBlue.copy(alpha = 0f),
                    iOSBlue.copy(alpha = 0.4f),
                    iOSBlue.copy(alpha = 0f)
                ),
                startY = currentY - 50,
                endY = currentY + 50
            ),
            start = Offset(0f, currentY),
            end = Offset(width, currentY),
            strokeWidth = 2f
        )
    }
}
