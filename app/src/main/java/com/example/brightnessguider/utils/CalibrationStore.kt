package com.example.brightnessguider.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.brightnessguider.model.CalibrationProfile
import com.example.brightnessguider.model.MonitorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.calibrationDataStore by preferencesDataStore(name = "calibration_prefs")

class CalibrationStore(private val context: Context) {
    private val BG_LUM = floatPreferencesKey("bg_lum")
    private val MAX_LUM = floatPreferencesKey("max_lum")
    private val MIN_LUM = floatPreferencesKey("min_lum")
    private val AMBIENT_LUX = floatPreferencesKey("ambient_lux")
    private val MONITOR_TYPE = stringPreferencesKey("monitor_type")
    private val MONITOR_SIZE = floatPreferencesKey("monitor_size")
    private val RELATIVE_MODE = booleanPreferencesKey("relative_mode")
    private val TIMESTAMP = longPreferencesKey("timestamp")

    val profile: Flow<CalibrationProfile?> = context.calibrationDataStore.data.map { prefs ->
        val typeStr = prefs[MONITOR_TYPE] ?: return@map null
        CalibrationProfile(
            backgroundLuminance = prefs[BG_LUM] ?: 0f,
            maxMonitorLuminance = prefs[MAX_LUM] ?: 0f,
            minMonitorLuminance = prefs[MIN_LUM] ?: 0f,
            ambientLuxAtCalibration = prefs[AMBIENT_LUX] ?: 0f,
            monitorType = MonitorType.valueOf(typeStr),
            monitorDiagonalInches = prefs[MONITOR_SIZE] ?: 24f,
            useRelativeOnlyMode = prefs[RELATIVE_MODE] ?: false,
            timestamp = prefs[TIMESTAMP] ?: 0L
        )
    }

    suspend fun saveProfile(profile: CalibrationProfile) {
        context.calibrationDataStore.edit { prefs ->
            prefs[BG_LUM] = profile.backgroundLuminance
            prefs[MAX_LUM] = profile.maxMonitorLuminance
            prefs[MIN_LUM] = profile.minMonitorLuminance
            prefs[AMBIENT_LUX] = profile.ambientLuxAtCalibration
            prefs[MONITOR_TYPE] = profile.monitorType.name
            prefs[MONITOR_SIZE] = profile.monitorDiagonalInches
            prefs[RELATIVE_MODE] = profile.useRelativeOnlyMode
            prefs[TIMESTAMP] = profile.timestamp
        }
    }

    suspend fun clear() {
        context.calibrationDataStore.edit { it.clear() }
    }
}
