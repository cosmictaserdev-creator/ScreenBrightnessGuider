package com.example.brightnessguider.model

enum class DistanceStatus {
    PERFECT,
    TOO_CLOSE,
    TOO_FAR
}

data class DistanceAdvice(
    val status: DistanceStatus,
    val totalDistanceCm: Float,
    val idealMinCm: Float,
    val idealMaxCm: Float,
    val faceToPhoneCm: Float?,
    val phoneToMonitorCm: Float?
) {
    companion object {
        fun compute(
            totalDistanceCm: Float,
            monitorDiagonalInches: Float
        ): DistanceAdvice {
            val idealMin = monitorDiagonalInches * 1.5f * 2.54f
            val idealMax = monitorDiagonalInches * 2.5f * 2.54f

            val status = when {
                totalDistanceCm < idealMin * 0.85f -> DistanceStatus.TOO_CLOSE
                totalDistanceCm > idealMax * 1.15f -> DistanceStatus.TOO_FAR
                else -> DistanceStatus.PERFECT
            }

            return DistanceAdvice(
                status = status,
                totalDistanceCm = totalDistanceCm,
                idealMinCm = idealMin,
                idealMaxCm = idealMax,
                faceToPhoneCm = null, // Set by ViewModel
                phoneToMonitorCm = null // Set by ViewModel
            )
        }
    }
}
