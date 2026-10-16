package com.example.brightnessguider.ui.camera

import android.graphics.RectF
import android.util.Size
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun MonitorBoxOverlay(
    detectedBox: RectF?,           // normalized 0–1 from ML Kit
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (detectedBox != null) 1f else 0f,
        animationSpec = tween(400), label = "boxAlpha"
    )

    // Animate box position smoothly
    val animLeft by animateFloatAsState(detectedBox?.left ?: 0.1f, label = "left")
    val animTop by animateFloatAsState(detectedBox?.top ?: 0.2f, label = "top")
    val animRight by animateFloatAsState(detectedBox?.right ?: 0.9f, label = "right")
    val animBottom by animateFloatAsState(detectedBox?.bottom ?: 0.8f, label = "bottom")

    // Scan line animation
    val scanProgress by rememberInfiniteTransition(label = "scan")
        .animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
            label = "scanLine"
        )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (alpha < 0.05f) return@Canvas

        val left = animLeft * size.width
        val top = animTop * size.height
        val right = animRight * size.width
        val bottom = animBottom * size.height
        val cornerLen = minOf(right - left, bottom - top) * 0.12f
        val stroke = 2.5.dp.toPx()
        val cyan = Color(0xFF00E5FF).copy(alpha = alpha)

        // Corner brackets
        listOf(
            // TL
            Offset(left, top + cornerLen) to Offset(left, top),
            Offset(left, top) to Offset(left + cornerLen, top),
            // TR
            Offset(right - cornerLen, top) to Offset(right, top),
            Offset(right, top) to Offset(right, top + cornerLen),
            // BL
            Offset(left, bottom - cornerLen) to Offset(left, bottom),
            Offset(left, bottom) to Offset(left + cornerLen, bottom),
            // BR
            Offset(right - cornerLen, bottom) to Offset(right, bottom),
            Offset(right, bottom) to Offset(right, bottom - cornerLen),
        ).forEach { (start, end) ->
            drawLine(cyan, start, end, strokeWidth = stroke,
                cap = StrokeCap.Round)
        }

        // Animated scan line inside box
        val scanY = top + (bottom - top) * scanProgress
        drawLine(
            color = cyan.copy(alpha = alpha * 0.4f),
            start = Offset(left + cornerLen, scanY),
            end = Offset(right - cornerLen, scanY),
            strokeWidth = 1.dp.toPx()
        )

        // Label
        drawContext.canvas.nativeCanvas.drawText(
            if (alpha > 0.8f) "MONITOR DETECTED" else "SCANNING...",
            left, top - 14f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (alpha * 220).toInt(), 0, 229, 255)
                textSize = 24f
                typeface = android.graphics.Typeface.MONOSPACE
            }
        )
    }
}
