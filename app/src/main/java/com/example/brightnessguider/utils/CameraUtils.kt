package com.example.brightnessguider.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

object CameraUtils {
    /**
     * Queries the maximum supported frame rate (FPS) for the back camera.
     */
    fun getMaxSupportedFps(context: Context): Int {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            // Use the back camera (usually the first ID, but we check for lens facing)
            val backCameraId = manager.cameraIdList.find { id ->
                val characteristics = manager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList[0]

            val characteristics = manager.getCameraCharacteristics(backCameraId)
            val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            
            fpsRanges?.maxOfOrNull { it.upper } ?: 30
        } catch (e: Exception) {
            30 // Default to 30 if something goes wrong
        }
    }
}
