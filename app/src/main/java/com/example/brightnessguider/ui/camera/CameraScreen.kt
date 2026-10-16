package com.example.brightnessguider.ui.camera

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.RectF
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brightnessguider.model.*
import com.example.brightnessguider.ui.components.StatsCard
import com.example.brightnessguider.ui.theme.*
import com.example.brightnessguider.viewmodel.LumoraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    profile: CalibrationProfile,
    onRecalibrate: () -> Unit,
    viewModel: LumoraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val brightnessAdvice by viewModel.brightnessAdvice.collectAsState()
    val distanceAdvice by viewModel.distanceAdvice.collectAsState()
    val monitorDetection by viewModel.monitorDetection.collectAsState()
    
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isAELocked by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    fun toggleLock(lock: Boolean) {
        isAELocked = lock
        activity?.window?.let { window ->
            if (lock) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        
        camera?.let { cam ->
            previewViewRef?.let { pv ->
                if (lock) {
                    val cx = (monitorDetection?.boundingBox?.centerX() ?: 0.5f) * pv.width
                    val cy = (monitorDetection?.boundingBox?.centerY() ?: 0.5f) * pv.height
                    val factory = pv.meteringPointFactory
                    val action = FocusMeteringAction.Builder(factory.createPoint(cx, cy))
                        .disableAutoCancel()
                        .build()
                    cam.cameraControl.startFocusAndMetering(action)
                } else {
                    cam.cameraControl.cancelFocusAndMetering()
                }
            }
        }
    }

    // Auto-lock logic
    LaunchedEffect(monitorDetection) {
        if (monitorDetection?.boundingBox != null && !isAELocked) {
            delay(2000)
            if (monitorDetection?.boundingBox != null) {
                toggleLock(true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(NavyDark)) {
        AnimatedContent(
            targetState = isLandscape,
            transitionSpec = {
                (fadeIn(tween(350)) + scaleIn(tween(350), 0.96f))
                    .togetherWith(fadeOut(tween(250)))
            },
            label = "orientationSwitch"
        ) { landscape ->
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(0.58f).fillMaxHeight()) {
                        CameraViewfinder(
                            onPreviewReady = { previewViewRef = it },
                            onCameraReady = { camera = it },
                            viewModel = viewModel
                        )
                        MonitorBoxOverlay(monitorDetection?.boundingBox)
                        ScanLineOverlay()
                    }
                    StatsCard(
                        brightnessAdvice = brightnessAdvice,
                        distanceAdvice = distanceAdvice,
                        isLandscape = true,
                        modifier = Modifier.weight(0.42f).fillMaxHeight()
                    )
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    CameraViewfinder(
                        onPreviewReady = { previewViewRef = it },
                        onCameraReady = { camera = it },
                        viewModel = viewModel
                    )
                    MonitorBoxOverlay(monitorDetection?.boundingBox)
                    ScanLineOverlay()
                    StatsCard(
                        brightnessAdvice = brightnessAdvice,
                        distanceAdvice = distanceAdvice,
                        isLandscape = false,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // AE Lock Button
        IconButton(
            onClick = { toggleLock(!isAELocked) },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .background(if (isAELocked) GreenNeon.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = if (isAELocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "AE Lock",
                tint = if (isAELocked) GreenNeon else Color.White
            )
        }
        
        if (isAELocked) {
            Text(
                "AE-L", 
                modifier = Modifier.padding(top = 16.dp, end = 64.dp).align(Alignment.TopEnd),
                color = GreenNeon,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CameraViewfinder(
    onPreviewReady: (PreviewView) -> Unit,
    onCameraReady: (Camera) -> Unit,
    viewModel: LumoraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    CameraPreview(modifier = Modifier.fillMaxSize()) { previewView ->
        onPreviewReady(previewView)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                        viewModel.analyzeFrame(image)
                    }
                }

            try {
                provider.unbindAll()
                val cam = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                onCameraReady(cam)
            } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }
}
