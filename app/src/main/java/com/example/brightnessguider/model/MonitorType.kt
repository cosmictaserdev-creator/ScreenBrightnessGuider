package com.example.brightnessguider.model

enum class MonitorType(val label: String, val description: String) {
    BUDGET("Budget / Cheap", "Brightness changes via contrast, not backlight"),
    IPS_VA("IPS / VA Panel", "True backlight dimming"),
    OLED("OLED / AMOLED", "Perfect blacks, per-pixel brightness"),
    UNKNOWN("Not Sure", "We'll use safe defaults")
}
