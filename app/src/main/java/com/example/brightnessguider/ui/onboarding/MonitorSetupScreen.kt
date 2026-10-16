package com.example.brightnessguider.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.model.MonitorType
import com.example.brightnessguider.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MonitorSetupScreen(onFinished: (Float, MonitorType) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var size by remember { mutableFloatStateOf(24f) }
    var selectedType by remember { mutableStateOf<MonitorType?>(null) }
    var briefingComplete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            1 -> SizeStep(size) { size = it }
            2 -> TypeStep(selectedType) { selectedType = it }
            3 -> BriefingStep { briefingComplete = true }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (step < 3) step++ else onFinished(size, selectedType ?: MonitorType.UNKNOWN)
            },
            enabled = (step == 1) || (step == 2 && selectedType != null) || (step == 3 && briefingComplete),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
        ) {
            Text("CONTINUE", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SizeStep(size: Float, onSizeChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What size is your monitor?", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size((size * 5).dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(2.dp, iOSBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("${size.toInt()}\"", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Slider(
            value = size,
            onValueChange = onSizeChange,
            valueRange = 18f..49f,
            steps = 31,
            colors = SliderDefaults.colors(thumbColor = iOSBlue, activeTrackColor = iOSBlue)
        )
        Text("18\" — 49\"", color = Color.White.copy(alpha = 0.4f))
    }
}

@Composable
fun TypeStep(selected: MonitorType?, onSelect: (MonitorType) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What kind of monitor do you have?", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        
        MonitorType.values().forEach { type ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(type) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == type) iOSBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                ),
                border = if (selected == type) androidx.compose.foundation.BorderStroke(2.dp, iOSBlue) else null
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(type.label, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(type.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun BriefingStep(onComplete: () -> Unit) {
    var timer by remember { mutableIntStateOf(5) }
    
    LaunchedEffect(Unit) {
        while(timer > 0) {
            delay(1000)
            timer--
        }
        onComplete()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pre-calibration briefing", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                BriefingItem("1️⃣", "Set monitor brightness to MAXIMUM (100%)", "Do this NOW before tapping Continue")
                Spacer(modifier = Modifier.height(20.dp))
                BriefingItem("2️⃣", "Make sure room lighting is normal", "Keep lights as you usually use them")
                Spacer(modifier = Modifier.height(20.dp))
                BriefingItem("3️⃣", "Hold phone to see full monitor & wall", "Background data is essential")
            }
        }
        
        if (timer > 0) {
            Text("Please read carefully (${timer}s)", color = iOSBlue, modifier = Modifier.padding(top = 24.dp))
        }
    }
}

@Composable
fun BriefingItem(icon: String, title: String, sub: String) {
    Row {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(sub, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}
