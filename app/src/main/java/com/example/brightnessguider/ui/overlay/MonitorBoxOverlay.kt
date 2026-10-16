package com.example.brightnessguider.ui.overlay

import android.graphics.Rect
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun MonitorBoxOverlay(
    monitorRect: Rect?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (monitorRect != null) 1f else 0f,
        animationSpec = tween(300)
    )
    
    val scanProgress by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        monitorRect ?: return@Canvas
        
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight
        
        val left = monitorRect.left * scaleX
        val top = monitorRect.top * scaleY
        val right = monitorRect.right * scaleX
        val bottom = monitorRect.bottom * scaleY
        
        val width = right - left
        val height = bottom - top
        
        val cornerColor = Color(0xFF00E5FF).copy(alpha = alpha)
        val strokeWidth = 3.dp.toPx()
        val cornerLen = minOf(width, height) * 0.15f

        // 1. Scanning Pulse Line
        val scanY = top + height * scanProgress
        drawLine(
            color = cornerColor.copy(alpha = alpha * 0.4f),
            start = Offset(left, scanY),
            end = Offset(right, scanY),
            strokeWidth = 2.dp.toPx()
        )

        // 2. Corner Brackets
        // Top-Left
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth)
        
        // Top-Right
        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLen, top), strokeWidth)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth)
        
        // Bottom-Left
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth)
        
        // Bottom-Right
        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth)

        // 3. Status Text
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((alpha * 255).toInt(), 0, 229, 255)
                textSize = 32f
                typeface = android.graphics.Typeface.MONOSPACE
                isFakeBoldText = true
            }
            drawText("MONITOR DETECTED", left, top - 15f, paint)
        }
    }
}
