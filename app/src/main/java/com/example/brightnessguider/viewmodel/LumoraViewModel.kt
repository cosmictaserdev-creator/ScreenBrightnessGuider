package com.example.brightnessguider.viewmodel

import android.app.Application
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brightnessguider.analyzer.LuminanceAnalyzer
import com.example.brightnessguider.analyzer.LuminanceReading
import com.example.brightnessguider.analyzer.MonitorDetection
import com.example.brightnessguider.analyzer.MonitorEdgeDetector
import com.example.brightnessguider.model.*
import com.example.brightnessguider.sensors.FaceMeshManager
import com.example.brightnessguider.sensors.LightSensorManager
import com.example.brightnessguider.utils.CalibrationStore
import com.example.brightnessguider.utils.HapticHelper
import com.example.brightnessguider.utils.OnboardingPrefs
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LumoraViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private val calibrationStore = CalibrationStore(context)
    val onboardingPrefs = OnboardingPrefs(context)
    val hapticHelper = HapticHelper(context)
    
    private val lightSensorManager = LightSensorManager(context)
    private val faceMeshManager = FaceMeshManager(context)
    private val monitorEdgeDetector = MonitorEdgeDetector()
    
    val profile = calibrationStore.profile.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    private val _luminance = MutableStateFlow<LuminanceReading?>(null)
    val luminance = _luminance.asStateFlow()
    
    private val _monitorDetection = MutableStateFlow<MonitorDetection?>(null)
    val monitorDetection = _monitorDetection.asStateFlow()
    
    private var lastKnownPhoneToMonitor = 50f // fallback cm
    
    val ambientLux = lightSensorManager.ambientLux
    val faceDistanceCm = faceMeshManager.faceDistanceCm
    
    val brightnessAdvice = combine(luminance, profile, ambientLux) { lum, prof, lux ->
        if (lum != null && prof != null) {
            BrightnessAdvice.compute(lum.smoothed, prof, lux)
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val distanceAdvice = combine(faceDistanceCm, monitorDetection, profile) { f2p, m2p, prof ->
        if (prof != null) {
            val p2m = m2p?.phoneToMonitorDistanceCm ?: lastKnownPhoneToMonitor
            if (m2p?.phoneToMonitorDistanceCm != null) lastKnownPhoneToMonitor = m2p.phoneToMonitorDistanceCm
            
            val total = (f2p ?: 35f) + p2m
            DistanceAdvice.compute(total, prof.monitorDiagonalInches).copy(
                faceToPhoneCm = f2p,
                phoneToMonitorCm = m2p?.phoneToMonitorDistanceCm
            )
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var frameCount = 0

    init {
        lightSensorManager.start()
        
        viewModelScope.launch {
            brightnessAdvice.collect { advice ->
                advice?.let { checkAndTriggerHaptics(it.status, distanceAdvice.value?.status ?: DistanceStatus.PERFECT) }
            }
        }
    }

    private var lastBrightnessStatus: BrightnessStatus? = null
    private var lastDistanceStatus: DistanceStatus? = null

    private fun checkAndTriggerHaptics(brightness: BrightnessStatus, distance: DistanceStatus) {
        if (brightness != lastBrightnessStatus) {
            if (brightness == BrightnessStatus.PERFECT) hapticHelper.doubleTick() else hapticHelper.tick()
            lastBrightnessStatus = brightness
        }
        if (distance != lastDistanceStatus) {
            hapticHelper.tick()
            lastDistanceStatus = distance
        }
    }

    fun saveProfile(prof: CalibrationProfile) {
        viewModelScope.launch {
            calibrationStore.saveProfile(prof)
        }
    }

    fun resetCalibration() {
        viewModelScope.launch {
            calibrationStore.clear()
        }
    }

    fun analyzeFrame(image: ImageProxy) {
        frameCount++
        
        val prof = profile.value
        
        // 1. Always run luminance (doesn't close image)
        val luminanceAnalyzer = LuminanceAnalyzer { reading ->
            _luminance.value = reading
        }
        
        // Since we have multiple analyzers that might need the image,
        // we'll run them sequentially or use a clone if needed.
        // For simplicity and performance, we'll alternate.
        
        if (frameCount % 10 == 0 && prof != null) {
            // ML Kit (closes image)
            monitorEdgeDetector.detect(image, prof.monitorDiagonalInches, 800f) { det ->
                _monitorDetection.value = det
            }
        } else if (frameCount % 15 == 5) {
            // MediaPipe (closes image)
            try {
                // MediaPipe requires a bitmap or MPImage. 
                // In a production app, we'd use a more efficient YUV to Bitmap converter.
                val bitmap = image.toBitmap() 
                val mpImage = BitmapImageBuilder(bitmap).build()
                faceMeshManager.detect(mpImage, System.currentTimeMillis())
            } catch (e: Exception) {}
            image.close()
        } else {
            // Luminance only (closes image)
            luminanceAnalyzer.analyze(image)
        }
    }

    override fun onCleared() {
        super.onCleared()
        lightSensorManager.stop()
        faceMeshManager.close()
    }
}

// Add to Utils or here
fun ImageProxy.toBitmap(): android.graphics.Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
