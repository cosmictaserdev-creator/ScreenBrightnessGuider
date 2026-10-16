package com.example.brightnessguider.ui.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brightnessguider.ui.theme.*

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit,
    isPermanentlyDenied: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = iOSBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Camera Access Required",
            style = Typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isPermanentlyDenied) {
                "Lumos needs camera access to analyze light. Please enable it in app settings."
            } else {
                "To measure your environment's light level, Lumos needs access to your rear camera."
            },
            style = Typography.bodyLarge,
            color = MutedTeal,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = iOSBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = if (isPermanentlyDenied) "Open Settings" else "Grant Permission",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
