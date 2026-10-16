package com.example.brightnessguider.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brightnessguider.ui.theme.iOSBlue
import kotlinx.coroutines.delay

@Composable
fun TipRotator(modifier: Modifier = Modifier) {
    val tips = listOf(
        "Match monitor to room light",
        "Keep distance at ~60cm",
        "Higher contrast = more strain",
        "Warm light is easier at night",
        "Blink often to reduce fatigue"
    )
    
    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(5000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    AnimatedContent(
        targetState = tips[currentTipIndex],
        transitionSpec = {
            fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { -it }
        },
        modifier = modifier,
        label = "tipSwitch"
    ) { tip ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, null, tint = iOSBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tip,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
