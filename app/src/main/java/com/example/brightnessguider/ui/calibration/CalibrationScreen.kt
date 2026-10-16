package com.example.brightnessguider.ui.calibration

import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.brightnessguider.analyzer.LuminanceAnalyzer
import com.example.brightnessguider.model.CalibrationProfile
import com.example.brightnessguider.model.MonitorType
import com.example.brightnessguider.ui.camera.ScanLineOverlay
import com.example.brightnessguider.ui.theme.*
import com.example.brightnessguider.sensors.LightSensorManager
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.Executors

enum class CalPhase {
    BACKGROUND,
    MONITOR_MAX,
    MONITOR_MIN,
    COMPLETE
}

@Composable
fun CalibrationScreen(
    monitorSize: Float,
    monitorType: MonitorType,
    onCalibrationFinished: (CalibrationProfile) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { LightSensorManager(context) }
    
    var phase by remember { mutableStateOf(CalPhase.BACKGROUND) }
    var isRecording by remember { mutableStateOf(false) }
    var showInstruction by remember { mutableStateOf(true) }
    
    val samples = remember { mutableStateListOf<Float>() }
    val goal = if (phase == CalPhase.BACKGROUND) 90 else 60
    
    var bgLum by remember { mutableFloatStateOf(0f) }
    var maxLum by remember { mutableFloatStateOf(0f) }
    var minLum by remember { mutableFloatStateOf(0f) }
    var ambientLuxAtCal by remember { mutableFloatStateOf(0f) }
    
    var currentLux by remember { mutableFloatStateOf(0f) }
    var currentLum by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        sensorManager.start()
        sensorManager.ambientLux.collectLatest { currentLux = it }
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), LuminanceAnalyzer { reading ->
                        currentLum = reading.smoothed
                        if (isRecording && samples.size < goal) {
                            samples.add(reading.smoothed)
                            if (samples.size >= goal) {
                                isRecording = false
                            }
                        }
                    })
                }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize().background(NavyDark)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        ScanLineOverlay()

        if (showInstruction) {
            InstructionCard(phase) { showInstruction = false }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FrostedBackground, RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when(phase) {
                                CalPhase.BACKGROUND -> "STEP 1: BACKGROUND"
                                CalPhase.MONITOR_MAX -> "STEP 2: MAX BRIGHTNESS"
                                CalPhase.MONITOR_MIN -> "STEP 3: MIN BRIGHTNESS"
                                else -> ""
                            },
                            color = iOSBlue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Reading: ${currentLum.toInt()} / 255",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LinearProgressIndicator(
                            progress = { samples.size.toFloat() / goal },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                            color = iOSBlue,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (samples.size < goal) {
                            Button(
                                onClick = { isRecording = true },
                                enabled = !isRecording,
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
                            ) {
                                Text(if (isRecording) "COLLECTING..." else "START CAPTURE", fontWeight = FontWeight.Black)
                            }
                        } else {
                            val median = samples.sorted()[samples.size / 2]
                            Button(
                                onClick = {
                                    when(phase) {
                                        CalPhase.BACKGROUND -> {
                                            bgLum = median
                                            ambientLuxAtCal = currentLux
                                            phase = CalPhase.MONITOR_MAX
                                        }
                                        CalPhase.MONITOR_MAX -> {
                                            maxLum = median
                                            phase = CalPhase.MONITOR_MIN
                                        }
                                        CalPhase.MONITOR_MIN -> {
                                            minLum = median
                                            val profile = CalibrationProfile(
                                                backgroundLuminance = bgLum,
                                                maxMonitorLuminance = maxLum,
                                                minMonitorLuminance = minLum,
                                                ambientLuxAtCalibration = ambientLuxAtCal,
                                                monitorType = monitorType,
                                                monitorDiagonalInches = monitorSize,
                                                useRelativeOnlyMode = (maxLum - minLum < 8f)
                                            )
                                            onCalibrationFinished(profile)
                                            phase = CalPhase.COMPLETE
                                        }
                                        else -> {}
                                    }
                                    samples.clear()
                                    showInstruction = true
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenNeon)
                            ) {
                                Text("CONTINUE", color = NavyDark, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        
        if (phase == CalPhase.COMPLETE) {
            SuccessOverlay()
        }
    }
}

@Composable
fun InstructionCard(phase: CalPhase, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.background(FrostedBackground, RoundedCornerShape(32.dp)).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val title = when(phase) {
                CalPhase.BACKGROUND -> "📷 Step 1 of 3\nScan the Room"
                CalPhase.MONITOR_MAX -> "🔆 Step 2 of 3\nCapture Bright Monitor"
                CalPhase.MONITOR_MIN -> "🔅 Step 3 of 3\nCapture Dark Monitor"
                else -> ""
            }
            val sub = when(phase) {
                CalPhase.BACKGROUND -> "Point your camera at the WALL or any area that does NOT include your monitor. Move it slowly."
                CalPhase.MONITOR_MAX -> "Your monitor should be at 100% brightness. Point camera at the CENTER and hold steady."
                CalPhase.MONITOR_MIN -> "Set monitor to MINIMUM (0%). Point camera at the CENTER and hold steady."
                else -> ""
            }

            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(24.dp))
            Text(sub, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
            ) {
                Text("GOT IT", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SuccessOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(NavyDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, null, tint = GreenNeon, modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("CALIBRATION COMPLETE!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Your environment is optimized", color = Color.White.copy(alpha = 0.6f))
        }
    }
}
