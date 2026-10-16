package com.example.brightnessguider.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brightnessguider.ui.camera.CameraScreen
import com.example.brightnessguider.ui.calibration.CalibrationScreen
import com.example.brightnessguider.ui.onboarding.OnboardingScreen
import com.example.brightnessguider.ui.onboarding.MonitorSetupScreen
import com.example.brightnessguider.ui.permission.PermissionScreen
import com.example.brightnessguider.viewmodel.LumoraViewModel
import com.example.brightnessguider.model.MonitorType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LumosApp(viewModel: LumoraViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val profile by viewModel.profile.collectAsState()
    val isOnboardingCompleted by viewModel.onboardingPrefs.isOnboardingCompleted.collectAsState(initial = null)
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var hasAskedPermission by remember { mutableStateOf(false) }

    // Transient setup state
    var tempSize by remember { mutableFloatStateOf(24f) }
    var tempType by remember { mutableStateOf<MonitorType?>(null) }

    if (isOnboardingCompleted == null) return 

    if (!cameraPermissionState.status.isGranted) {
        PermissionScreen(
            onRequestPermission = {
                if (!hasAskedPermission || cameraPermissionState.status.shouldShowRationale) {
                    cameraPermissionState.launchPermissionRequest()
                    hasAskedPermission = true
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            },
            isPermanentlyDenied = hasAskedPermission && !cameraPermissionState.status.shouldShowRationale
        )
    } else if (isOnboardingCompleted == false) {
        OnboardingScreen(onFinished = {
            scope.launch { viewModel.onboardingPrefs.setOnboardingCompleted() }
        })
    } else if (profile == null) {
        if (tempType == null) {
            MonitorSetupScreen(onFinished = { size, type ->
                tempSize = size
                tempType = type
            })
        } else {
            CalibrationScreen(
                monitorSize = tempSize,
                monitorType = tempType!!,
                onCalibrationFinished = { prof ->
                    viewModel.saveProfile(prof)
                }
            )
        }
    } else {
        CameraScreen(
            profile = profile!!,
            onRecalibrate = {
                viewModel.resetCalibration()
            }
        )
    }
}
