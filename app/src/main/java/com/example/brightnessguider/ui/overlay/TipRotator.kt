package com.example.brightnessguider.ui.overlay

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.brightnessguider.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun TipRotator() {
    val tips = listOf(
        "Your eyes adjust — check again after 30 seconds",
        "Match monitor brightness to your surroundings",
        "Reduce blue light in the evenings",
        "Keep monitor at arm's length for best results"
    )

    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = tips[currentTipIndex],
            transitionSpec = {
                fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
            },
            label = "tipAnimation"
        ) { tip ->
            Text(
                text = tip,
                style = Typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                textAlign = TextAlign.Center
            )
        }
    }
}
