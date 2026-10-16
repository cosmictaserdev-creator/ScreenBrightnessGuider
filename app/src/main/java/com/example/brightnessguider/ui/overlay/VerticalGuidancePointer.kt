package com.example.brightnessguider.ui.overlay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import com.example.brightnessguider.ui.theme.WhiteGlowing

@Composable
fun VerticalGuidancePointer(yPercent: Float) {
    // yPercent: 0 is top, 1 is bottom. 0.5 is center.
    val isTooHigh = yPercent < 0.3f
    val isTooLow = yPercent > 0.7f

    if (!isTooHigh && !isTooLow) return

    val infiniteTransition = rememberInfiniteTransition(label = "arrowAnim")
    val translation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 200.dp),
        contentAlignment = if (isTooHigh) Alignment.TopCenter else Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = if (isTooHigh) translation.dp else -translation.dp)
        ) {
            if (isTooHigh) {
                Text("↓ MOVE DOWN", color = iOSBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box(modifier = Modifier.size(2.dp, 40.dp).background(iOSBlue))
            } else {
                Box(modifier = Modifier.size(2.dp, 40.dp).background(iOSBlue))
                Text("↑ MOVE UP", color = iOSBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
