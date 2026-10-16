package com.example.brightnessguider.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.model.BrightnessAdvice
import com.example.brightnessguider.model.DistanceAdvice
import com.example.brightnessguider.model.StatusHeadline
import com.example.brightnessguider.ui.camera.DistancePlane3D
import com.example.brightnessguider.ui.theme.*

@Composable
fun StatsCard(
    brightnessAdvice: BrightnessAdvice?,
    distanceAdvice: DistanceAdvice?,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedBackground, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .padding(24.dp)
    ) {
        // 1. Environment & Lux
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = brightnessAdvice?.environmentLabel?.uppercase() ?: "ANALYZING...",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            
            Badge(containerColor = Color.White.copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LightMode, null, tint = Color.Yellow, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${brightnessAdvice?.ambientLux?.toInt() ?: 0} LUX", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. Brightness Status
        brightnessAdvice?.let { advice ->
            val headline = when(advice.status) {
                com.example.brightnessguider.model.BrightnessStatus.PERFECT -> StatusHeadline.PERFECT
                com.example.brightnessguider.model.BrightnessStatus.TOO_BRIGHT -> StatusHeadline.TOO_BRIGHT
                com.example.brightnessguider.model.BrightnessStatus.TOO_DARK -> StatusHeadline.TOO_DARK
            }
            
            Column {
                AnimatedContent(targetState = headline, label = "statusHeadline") { target ->
                    Text(target.text, color = target.color, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
                
                if (advice.status != com.example.brightnessguider.model.BrightnessStatus.PERFECT) {
                    Text(
                        text = if (advice.status == com.example.brightnessguider.model.BrightnessStatus.TOO_BRIGHT) 
                            "Reduce by ~${advice.adjustmentPercent}%" else "Increase by ~${advice.adjustmentPercent}%",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // 3. Brightness Meter
        brightnessAdvice?.let { 
            BrightnessMeter(ratio = it.ratio, idealRange = it.idealRatioRange)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 4. Distance System
        if (distanceAdvice != null) {
            DistancePlane3D(
                currentDistanceCm = distanceAdvice.totalDistanceCm,
                idealMinCm = distanceAdvice.idealMinCm,
                idealMaxCm = distanceAdvice.idealMaxCm
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Position yourself in front of the monitor\nto measure distance", 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.White.copy(alpha = 0.3f))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 5. Rotating Tip
        TipRotator()
    }
}
