package com.example.brightnessguider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.brightnessguider.ui.LumosApp
import com.example.brightnessguider.ui.theme.LumosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumosTheme {
                LumosApp()
            }
        }
    }
}
