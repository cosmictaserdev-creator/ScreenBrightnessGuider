package com.example.brightnessguider.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun FaceMeshOverlay(
    faceDetected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!faceDetected) return

    Canvas(modifier = modifier.fillMaxSize()) {
        // Since we don't have the full landmarks list here for brevity,
        // we'll draw a subtle indicator or just eye dots if we had the data.
        // For now, drawing a very subtle glow if face is detected.
        
        drawCircle(
            color = Color(0xFF06D6A0).copy(alpha = 0.05f),
            radius = 20f,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}
