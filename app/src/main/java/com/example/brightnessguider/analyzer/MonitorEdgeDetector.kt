package com.example.brightnessguider.analyzer

import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import androidx.camera.core.ImageProxy
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import kotlin.math.sqrt

data class MonitorDetection(
    val boundingBox: RectF?,
    val phoneToMonitorDistanceCm: Float?
)

class MonitorEdgeDetector {
    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableMultipleObjects()
        .build()

    private val detector = ObjectDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun detect(
        image: ImageProxy,
        monitorDiagonalInches: Float,
        focalLengthPx: Float,
        onResult: (MonitorDetection) -> Unit
    ) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { objects ->
                // Pick the largest object as the monitor
                val largest = objects.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                if (largest != null) {
                    val box = largest.boundingBox
                    val normalizedBox = RectF(
                        box.left.toFloat() / image.width,
                        box.top.toFloat() / image.height,
                        box.right.toFloat() / image.width,
                        box.bottom.toFloat() / image.height
                    )
                    
                    val distance = estimatePhoneToMonitorDistance(
                        boundingBoxWidthPx = box.width().toFloat(),
                        imageWidthPx = image.width.toFloat(),
                        monitorDiagonalInches = monitorDiagonalInches,
                        focalLengthPx = focalLengthPx
                    )
                    onResult(MonitorDetection(normalizedBox, distance))
                } else {
                    onResult(MonitorDetection(null, null))
                }
            }
            .addOnFailureListener {
                onResult(MonitorDetection(null, null))
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun estimatePhoneToMonitorDistance(
        boundingBoxWidthPx: Float,
        imageWidthPx: Float,
        monitorDiagonalInches: Float,
        focalLengthPx: Float
    ): Float {
        // Convert diagonal to width (16:9 assumption)
        val diagonalCm = monitorDiagonalInches * 2.54f
        val realWidthCm = diagonalCm * (16f / sqrt(337.0).toFloat())

        return (realWidthCm * focalLengthPx) / boundingBoxWidthPx
    }
}
