package com.example.brightnessguider.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.ui.theme.*

@Composable
fun MonitorSizeScreen(onFinished: (Float) -> Unit) {
    var size by remember { mutableFloatStateOf(24f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What size is your monitor?",
            style = Typography.displayLarge.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This helps Lumos calculate your ideal viewing distance.",
            style = Typography.bodyLarge,
            color = MutedTeal,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "${size.toInt()}\"",
            style = Typography.displayLarge.copy(fontSize = 72.sp, color = iOSBlue),
            fontWeight = FontWeight.Black
        )
        
        Slider(
            value = size,
            onValueChange = { size = it },
            valueRange = 18f..49f,
            steps = 31,
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            colors = SliderDefaults.colors(
                thumbColor = iOSBlue,
                activeTrackColor = iOSBlue,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { onFinished(size) },
            colors = ButtonDefaults.buttonColors(containerColor = iOSBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("Complete Setup", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
