package com.example.brightnessguider.ui.overlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.model.DistanceStatus
import com.example.brightnessguider.ui.theme.*

@Composable
fun DistancePlane3D(
    currentDistanceCm: Float,
    idealMinCm: Float,
    idealMaxCm: Float,
    modifier: Modifier = Modifier
) {
    val animatedDistance by animateFloatAsState(
        targetValue = currentDistanceCm,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 80f),
        label = "distanceAnim"
    )

    val zoneColor by animateColorAsState(
        targetValue = when {
            animatedDistance < idealMinCm * 0.85f -> RedNeon
            animatedDistance > idealMaxCm * 1.15f -> AmberNeon
            else -> GreenNeon
        },
        animationSpec = tween(600),
        label = "zoneColorAnim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val w = size.width
            val h = size.height
            val horizon = h * 0.35f

            // Perspective grid
            val gridColor = zoneColor.copy(alpha = 0.25f)
            val vanishX = w / 2f

            // Horizontal lines
            for (i in 0..5) {
                val t = i / 5f
                val y = horizon + (h - horizon) * t
                val xLeft = vanishX - (vanishX * (0.1f + 0.9f * t))
                val xRight = vanishX + (vanishX * (0.1f + 0.9f * t))
                drawLine(gridColor, Offset(xLeft, y), Offset(xRight, y), strokeWidth = 1.5f)
            }

            // Vertical lines
            for (i in -4..4) {
                val xBottom = vanishX + (w * 0.12f * i)
                drawLine(gridColor, Offset(vanishX, horizon), Offset(xBottom, h), strokeWidth = 1.5f)
            }

            // Target zone band
            val maxDistDisplay = idealMaxCm * 2.0f
            val idealMinT = (idealMinCm / maxDistDisplay).coerceIn(0f, 1f)
            val idealMaxT = (idealMaxCm / maxDistDisplay).coerceIn(0f, 1f)
            val bandTop = horizon + (h - horizon) * idealMinT
            val bandBottom = horizon + (h - horizon) * idealMaxT
            drawRect(
                color = GreenNeon.copy(alpha = 0.08f),
                topLeft = Offset(0f, bandTop),
                size = androidx.compose.ui.geometry.Size(w, bandBottom - bandTop)
            )

            // User indicator dot (Pointer)
            val t = (animatedDistance / maxDistDisplay).coerceIn(0f, 1f)
            val dotY = horizon + (h - horizon) * t
            val dotX = vanishX
            val dotRadius = 14f + (1f - t) * 8f

            // Pointer Glow
            drawCircle(zoneColor.copy(alpha = 0.2f), dotRadius * 2.5f, Offset(dotX, dotY))
            
            // Pointer Body (3D-ish look)
            drawCircle(zoneColor, dotRadius, Offset(dotX, dotY))
            drawCircle(Color.White.copy(alpha = 0.8f), dotRadius * 0.4f, Offset(dotX - dotRadius * 0.2f, dotY - dotRadius * 0.2f))

            // Directional Guidance Text & Arrows
            val statusText = when {
                animatedDistance < idealMinCm * 0.85f -> "MOVE BACK"
                animatedDistance > idealMaxCm * 1.15f -> "MOVE CLOSER"
                else -> "PERFECT DISTANCE"
            }

            // Draw status overlay on grid
            if (statusText != "PERFECT DISTANCE") {
                val arrowY = if (statusText == "MOVE BACK") dotY + 40f else dotY - 40f
                val arrowAlpha = 0.6f + (Math.sin(System.currentTimeMillis() / 200.0).toFloat() * 0.4f)
                
                // Simplified Arrow drawing (could be path)
                drawCircle(zoneColor.copy(alpha = arrowAlpha), 10f, Offset(dotX, arrowY))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = when {
                    animatedDistance < idealMinCm * 0.85f -> "↓ MOVE BACK"
                    animatedDistance > idealMaxCm * 1.15f -> "↑ MOVE CLOSER"
                    else -> "✓ PERFECT DISTANCE"
                },
                style = Typography.labelSmall.copy(
                    color = zoneColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = "%.0f cm away · Ideal: %.0f–%.0f cm".format(
                currentDistanceCm, idealMinCm, idealMaxCm
            ),
            style = Typography.labelSmall.copy(color = WhiteGlowing.copy(alpha = 0.7f)),
            modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)
        )
    }
}
