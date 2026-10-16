package com.example.brightnessguider.ui.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.model.DistanceStatus

@Composable
fun DistancePlane3D(
    currentDistanceCm: Float,
    idealMinCm: Float,
    idealMaxCm: Float,
    modifier: Modifier = Modifier
) {
    val animatedDist by animateFloatAsState(
        targetValue = currentDistanceCm,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 50f),
        label = "distance"
    )

    val status = when {
        animatedDist < idealMinCm * 0.85f -> DistanceStatus.TOO_CLOSE
        animatedDist > idealMaxCm * 1.15f -> DistanceStatus.TOO_FAR
        else -> DistanceStatus.PERFECT
    }

    val statusColor by animateColorAsState(
        targetValue = when (status) {
            DistanceStatus.TOO_CLOSE -> Color(0xFFFF4D6D)
            DistanceStatus.TOO_FAR -> Color(0xFFFFB703)
            DistanceStatus.PERFECT -> Color(0xFF06D6A0)
        },
        animationSpec = tween(600), label = "color"
    )

    val arrowBounce by rememberInfiniteTransition(label = "bounce")
        .animateFloat(
            initialValue = 0f, targetValue = 14f,
            animationSpec = infiniteRepeatable(
                tween(600, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = "arrow"
        )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arrow shown ABOVE grid when too far (move forward)
        if (status == DistanceStatus.TOO_FAR) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Move closer",
                tint = statusColor,
                modifier = Modifier.size(36.dp).offset(y = arrowBounce.dp)
            )
            Text("MOVE CLOSER", color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
        }

        // 3D perspective grid canvas
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val w = size.width
            val h = size.height
            val horizon = h * 0.3f
            val vanishX = w / 2f
            val gridColor = statusColor.copy(alpha = 0.2f)

            // Horizontal recession lines
            for (i in 0..6) {
                val t = i / 6f
                val y = horizon + (h - horizon) * t
                val spread = 0.05f + 0.95f * t
                drawLine(gridColor,
                    Offset(vanishX - vanishX * spread, y),
                    Offset(vanishX + vanishX * spread, y),
                    strokeWidth = 1.dp.toPx())
            }

            // Vertical lines converging to vanishing point
            for (i in -5..5) {
                val xBottom = vanishX + (w * 0.1f * i)
                drawLine(gridColor,
                    Offset(vanishX, horizon),
                    Offset(xBottom, h),
                    strokeWidth = 1.dp.toPx())
            }

            // Ideal zone band (green highlight)
            val maxDist = idealMaxCm * 1.6f
            val bandTopT = (idealMinCm / maxDist).coerceIn(0f, 1f)
            val bandBotT = (idealMaxCm / maxDist).coerceIn(0f, 1f)
            val bandTop = horizon + (h - horizon) * bandTopT
            val bandBot = horizon + (h - horizon) * bandBotT
            drawRect(
                color = Color(0xFF06D6A0).copy(alpha = 0.08f),
                topLeft = Offset(0f, bandTop),
                size = androidx.compose.ui.geometry.Size(w, bandBot - bandTop)
            )
            // "IDEAL ZONE" label
            drawContext.canvas.nativeCanvas.drawText(
                "IDEAL ZONE",
                8f, bandTop - 4f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(150, 6, 214, 160)
                    textSize = 20f
                    typeface = android.graphics.Typeface.MONOSPACE
                }
            )

            // User position dot
            val dotT = (animatedDist / maxDist).coerceIn(0f, 1f)
            val dotY = horizon + (h - horizon) * dotT
            val dotRadius = 10.dp.toPx() * (1f - dotT * 0.4f)

            // Glow rings
            drawCircle(statusColor.copy(alpha = 0.1f), dotRadius * 3f, Offset(vanishX, dotY))
            drawCircle(statusColor.copy(alpha = 0.3f), dotRadius * 1.8f, Offset(vanishX, dotY))
            drawCircle(statusColor, dotRadius, Offset(vanishX, dotY))
        }

        // Arrow shown BELOW grid when too close (move back)
        if (status == DistanceStatus.TOO_CLOSE) {
            Spacer(Modifier.height(4.dp))
            Text("MOVE BACK", color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp)
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Move back",
                tint = statusColor,
                modifier = Modifier.size(36.dp).offset(y = (-arrowBounce).dp)
            )
        }

        if (status == DistanceStatus.PERFECT) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = Color(0xFF06D6A0),
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Perfect distance",
                    color = Color(0xFF06D6A0),
                    style = MaterialTheme.typography.labelSmall)
            }
        }

        // Distance label
        Spacer(Modifier.height(6.dp))
        Text(
            "%.0f cm away  ·  Ideal: %.0f – %.0f cm".format(
                currentDistanceCm, idealMinCm, idealMaxCm
            ),
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
