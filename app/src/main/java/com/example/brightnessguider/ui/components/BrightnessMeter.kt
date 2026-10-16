package com.example.brightnessguider.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.ui.theme.iOSBlue

@Composable
fun BrightnessMeter(
    ratio: Float,
    idealRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val normalizedRatio = (ratio / 3.0f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedRatio, label = "progress")
    
    val minT = (idealRange.start / 3.0f).coerceIn(0f, 1f)
    val maxT = (idealRange.endInclusive / 3.0f).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("DIM", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
            Text("BRIGHT", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            // Ideal Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxT - minT)
                    .fillMaxHeight()
                    .offset(x = (400 * minT).dp) // This is a rough estimation, better to use BoxWithConstraints
                    .background(Color(0xFF06D6A0).copy(alpha = 0.3f))
            )
            
            // Current Level
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(iOSBlue)
            )
        }
    }
}
