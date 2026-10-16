package com.example.brightnessguider.model

import androidx.compose.ui.graphics.Color

enum class BrightnessStatus {
    PERFECT,
    TOO_BRIGHT,
    TOO_DARK
}

enum class StatusHeadline(val text: String, val color: Color) {
    PERFECT("✓ PERFECT", Color(0xFF06D6A0)),
    TOO_BRIGHT("↓ DECREASE BRIGHTNESS", Color(0xFFFF4D6D)),
    TOO_DARK("↑ INCREASE BRIGHTNESS", Color(0xFFFFB703))
}

data class BrightnessAdvice(
    val status: BrightnessStatus,
    val ratio: Float,
    val idealRatioRange: ClosedFloatingPointRange<Float>,
    val adjustmentPercent: Int,
    val ambientLux: Float,
    val environmentLabel: String
) {
    companion object {
        fun compute(
            currentLuminance: Float,
            profile: CalibrationProfile,
            ambientLux: Float
        ): BrightnessAdvice {
            val monitorType = profile.monitorType
            
            // Relative ratio: how much brighter is monitor vs background?
            val ratio = currentLuminance / (profile.backgroundLuminance + 1f)

            // Ideal ratio based on ambient lux
            // Dark room -> ratio should be ~1.2
            // Bright room -> ratio can be ~2.5
            val luxNormalized = (ambientLux / 1000f).coerceIn(0f, 1f)
            val idealRatioMin = 1.2f + (luxNormalized * 0.8f)  // 1.2 -> 2.0
            val idealRatioMax = 1.8f + (luxNormalized * 1.2f)  // 1.8 -> 3.0

            // Tolerance based on monitor type
            val tolerance = when (monitorType) {
                MonitorType.BUDGET -> 0.25f
                MonitorType.IPS_VA -> 0.15f
                MonitorType.OLED -> 0.10f
                MonitorType.UNKNOWN -> 0.20f
            }

            val status = when {
                ratio < idealRatioMin - tolerance -> BrightnessStatus.TOO_DARK
                ratio > idealRatioMax + tolerance -> BrightnessStatus.TOO_BRIGHT
                else -> BrightnessStatus.PERFECT
            }

            val adjustmentPercent = when {
                status == BrightnessStatus.TOO_BRIGHT ->
                    ((ratio - idealRatioMax) / idealRatioMax * 100f).toInt().coerceIn(1, 100)
                status == BrightnessStatus.TOO_DARK ->
                    ((idealRatioMin - ratio) / idealRatioMin * 100f).toInt().coerceIn(1, 100)
                else -> 0
            }

            return BrightnessAdvice(
                status = status,
                ratio = ratio,
                idealRatioRange = idealRatioMin..idealRatioMax,
                adjustmentPercent = adjustmentPercent,
                ambientLux = ambientLux,
                environmentLabel = when {
                    ambientLux < 50 -> "Dark Room"
                    ambientLux < 200 -> "Dim Room"
                    ambientLux < 500 -> "Normal Indoor"
                    ambientLux < 1000 -> "Bright Indoor"
                    else -> "Very Bright"
                }
            )
        }
    }
}
