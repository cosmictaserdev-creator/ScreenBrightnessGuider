package com.example.brightnessguider.model

data class CalibrationProfile(
    val backgroundLuminance: Float,
    val maxMonitorLuminance: Float,
    val minMonitorLuminance: Float,
    val ambientLuxAtCalibration: Float,
    val monitorType: MonitorType,
    val monitorDiagonalInches: Float,
    val useRelativeOnlyMode: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val contrastRatio: Float
        get() = if (minMonitorLuminance > 0) maxMonitorLuminance / minMonitorLuminance else 1f
}
