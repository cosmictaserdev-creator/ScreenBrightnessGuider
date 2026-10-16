package com.example.brightnessguider.sensors

import android.content.Context
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class FaceMeshManager(context: Context) {

    // Average interpupillary distance in cm
    private val KNOWN_IPD_CM = 6.3f

    private val faceLandmarker: FaceLandmarker? by lazy {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setResultListener { result, image ->
                    processResult(result, image)
                }
                .build()

            FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            null
        }
    }

    private var focalLengthPx: Float = 800f // calibrated at init
    private val _faceDistanceCm = MutableStateFlow<Float?>(null)
    val faceDistanceCm = _faceDistanceCm.asStateFlow()

    private fun processResult(result: FaceLandmarkerResult, image: MPImage) {
        val landmarks = result.faceLandmarks().firstOrNull() ?: return

        // Left eye outer corner = landmark 33
        // Right eye outer corner = landmark 263
        val leftEye = landmarks[33]
        val rightEye = landmarks[263]

        val imageWidthPx = image.width.toFloat()
        val ipdPx = abs(rightEye.x() - leftEye.x()) * imageWidthPx

        if (ipdPx > 10f) {
            val distanceCm = (KNOWN_IPD_CM * focalLengthPx) / ipdPx
            _faceDistanceCm.value = distanceCm
        }
    }

    fun detect(image: MPImage, timestamp: Long) {
        faceLandmarker?.detectAsync(image, timestamp)
    }

    fun initFocalLength(sensorWidthMm: Float, focalLengthMm: Float, imageWidthPx: Int) {
        focalLengthPx = (focalLengthMm / sensorWidthMm) * imageWidthPx
    }
    
    fun close() {
        faceLandmarker?.close()
    }
}
