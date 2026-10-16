package com.example.brightnessguider.ui.overlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brightnessguider.ui.theme.*

@Composable
fun LightMeterBar(score: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(500),
        label = "progress"
    )

    val color by animateColorAsState(
        targetValue = when {
            score < 0.2f -> iOSBlue
            score < 0.5f -> GreenNeon
            score < 0.8f -> AmberNeon
            else -> RedNeon
        },
        animationSpec = tween(500),
        label = "color"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val barWidth = maxWidth
            
            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            )
            
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            
            // Glowing dot at the end
            Box(
                modifier = Modifier
                    .offset(x = (barWidth * animatedProgress) - 6.dp)
                    .size(12.dp)
                    .background(WhiteGlowing, CircleShape)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("DIM", style = Typography.labelSmall)
            Text("BRIGHT", style = Typography.labelSmall)
        }
    }
}
