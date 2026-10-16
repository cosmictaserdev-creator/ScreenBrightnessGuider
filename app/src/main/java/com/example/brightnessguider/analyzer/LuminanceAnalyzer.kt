package com.example.brightnessguider.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

data class LuminanceReading(
    val raw: Float,
    val smoothed: Float
)

class LuminanceAnalyzer(
    private val onResult: (LuminanceReading) -> Unit
) : ImageAnalysis.Analyzer {

    private val frameBuffer = ArrayDeque<Float>(30)

    override fun analyze(image: ImageProxy) {
        // ONLY use Y plane — never touch U/V planes
        val yPlane = image.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        var sum = 0L
        var count = 0

        val width = image.width
        val height = image.height

        // Sample every 8th pixel for performance
        for (row in 0 until height step 8) {
            for (col in 0 until width step 8) {
                val index = row * rowStride + col
                if (index < buffer.limit()) {
                    sum += buffer.get(index).toInt() and 0xFF
                    count++
                }
            }
        }

        val raw = if (count > 0) sum.toFloat() / count else 0f

        // Rolling median over last 30 frames
        synchronized(frameBuffer) {
            if (frameBuffer.size >= 30) frameBuffer.removeFirst()
            frameBuffer.addLast(raw)
            val sorted = frameBuffer.sorted()
            val smoothed = if (sorted.isNotEmpty()) sorted[sorted.size / 2] else 0f
            onResult(LuminanceReading(raw = raw, smoothed = smoothed))
        }

        image.close()
    }
}
