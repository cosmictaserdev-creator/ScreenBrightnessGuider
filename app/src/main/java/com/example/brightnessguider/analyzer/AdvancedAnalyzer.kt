package com.example.brightnessguider.analyzer

import android.graphics.Rect
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlin.math.sqrt

enum class SceneContext(val label: String) {
    OUTDOOR("OUTDOOR"),
    OFFICE("BRIGHT OFFICE"),
    DARK("DARK ROOM"),
    INDOOR("NORMAL INDOOR")
}

enum class MonitorType(val label: String) {
    PWM_DIMMING("PWM Monitor (Flicker)"),
    CONTRAST_ONLY("Contrast-only Monitor"),
    TRUE_BACKLIGHT("True Backlight Monitor"),
    UNKNOWN("Analyzing Monitor...")
}

data class ColorTempReading(
    val kelvin: Float,
    val label: String
)

data class AnalysisResult(
    val luminance: Float,
    val sceneContext: SceneContext,
    val monitorType: MonitorType,
    val colorTemp: ColorTempReading,
    val monitorWidthPx: Float?,
    val monitorBoundingBox: Rect? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)

class AdvancedAnalyzer(
    private val onResult: (AnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)
            .build()
    )

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .build()
    )

    private var frameCount = 0
    private var lastSceneContext = SceneContext.INDOOR
    private var lastMonitorWidthPx: Float? = null
    private var lastBoundingBox: Rect? = null
    
    private val recentLuminance = ArrayDeque<Float>(120)
    private var lastColorTemp = ColorTempReading(6500f, "Neutral")

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        val yStride = yPlane.rowStride
        val uvStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride
        
        val width = image.width
        val height = image.height
        
        var ySum = 0L
        var rSum = 0f; var gSum = 0f; var bSum = 0f
        var count = 0
        var rgbCount = 0
        
        // Sampling areas
        val startRow = height / 4
        val endRow = height * 3 / 4
        val startCol = width / 4
        val endCol = width * 3 / 4

        // 1. Luminance & Color Temp Sampling
        for (row in startRow until endRow step 8) {
            for (col in startCol until endCol step 8) {
                // Y (Luminance) - index 0
                val y = (yBuffer.get(row * yStride + col).toInt() and 0xFF).toFloat()
                ySum += y.toLong()
                count++

                // RGB Estimation for Color Temp (every 16th for performance)
                if (row % 16 == 0 && col % 16 == 0) {
                    val uvIndex = (row / 2) * uvStride + (col / 2) * uvPixelStride
                    val u = (uBuffer.get(uvIndex).toInt() and 0xFF) - 128f
                    val v = (vBuffer.get(uvIndex).toInt() and 0xFF) - 128f
                    
                    rSum += y + 1.402f * v
                    gSum += y - 0.344f * u - 0.714f * v
                    bSum += y + 1.772f * u
                    rgbCount++
                }
            }
        }
        
        val luminance = if (count > 0) ySum.toFloat() / count else 0f
        recentLuminance.addLast(luminance)
        if (recentLuminance.size > 120) recentLuminance.removeFirst()

        // Color Temp calculation
        if (rgbCount > 0) {
            val r = (rSum / rgbCount).coerceIn(0f, 255f)
            val g = (gSum / rgbCount).coerceIn(0f, 255f)
            val b = (bSum / rgbCount).coerceIn(0f, 255f)
            lastColorTemp = estimateColorTemp(r, g, b)
        }

        // 2. ML Kit Tasks every 15 frames
        if (frameCount % 15 == 0) {
            val mediaImage = image.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                
                labeler.process(inputImage).addOnSuccessListener { labels ->
                    lastSceneContext = when {
                        labels.any { it.text.contains("outdoor", true) || it.text.contains("sunlight", true) } -> SceneContext.OUTDOOR
                        labels.any { it.text.contains("office", true) || it.text.contains("desk", true) } -> SceneContext.OFFICE
                        labels.any { it.text.contains("dark", true) || it.text.contains("night", true) } -> SceneContext.DARK
                        else -> SceneContext.INDOOR
                    }
                }

                objectDetector.process(inputImage).addOnSuccessListener { objects ->
                    val obj = objects.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                    lastMonitorWidthPx = obj?.boundingBox?.width()?.toFloat()
                    lastBoundingBox = obj?.boundingBox
                }
            }
        }

        val monitorType = classifyMonitor(recentLuminance.toList())

        val rotation = image.imageInfo.rotationDegrees
        val (finalWidth, finalHeight) = if (rotation == 90 || rotation == 270) {
            height to width
        } else {
            width to height
        }

        onResult(AnalysisResult(
            luminance = luminance,
            sceneContext = lastSceneContext,
            monitorType = monitorType,
            colorTemp = lastColorTemp,
            monitorWidthPx = lastMonitorWidthPx,
            monitorBoundingBox = lastBoundingBox,
            imageWidth = finalWidth,
            imageHeight = finalHeight
        ))
        
        frameCount++
        image.close()
    }

    private fun estimateColorTemp(r: Float, g: Float, b: Float): ColorTempReading {
        val x = r / (r + g + b + 0.001f)
        val y = g / (r + g + b + 0.001f)
        val n = (x - 0.3320f) / (0.1858f - y)
        val cct = (449f * n * n * n + 3525f * n * n + 6823.3f * n + 5520.33f)
        
        return ColorTempReading(
            kelvin = cct.coerceIn(2000f, 10000f),
            label = when {
                cct < 3500 -> "Warm · Low Strain"
                cct < 5500 -> "Neutral · Optimal"
                else -> "Cool · High Blue Light"
            }
        )
    }

    private fun classifyMonitor(frames: List<Float>): MonitorType {
        if (frames.size < 60) return MonitorType.UNKNOWN
        val mean = frames.average().toFloat()
        val variance = frames.map { (it - mean) * (it - mean) }.average().toFloat()
        val stdDev = sqrt(variance)

        // Count zero crossings for PWM detection
        var crossings = 0
        for (i in 1 until frames.size) {
            if ((frames[i] - mean) * (frames[i-1] - mean) < 0) crossings++
        }
        val oscillationScore = crossings.toFloat() / frames.size

        return when {
            oscillationScore > 0.4f -> MonitorType.PWM_DIMMING
            stdDev < 3f -> MonitorType.CONTRAST_ONLY
            stdDev in 3f..12f -> MonitorType.TRUE_BACKLIGHT
            else -> MonitorType.TRUE_BACKLIGHT // Default assumption
        }
    }
}
