package com.maru.videoeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val MaruColors = darkColorScheme(
    primary = Color(0xFFA56BFF),
    secondary = Color(0xFF5AD6E8),
    background = Color(0xFF10111A),
    surface = Color(0xFF1B1D29),
    onBackground = Color(0xFFF7F5FF),
    onSurface = Color(0xFFF7F5FF)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = MaruColors) {
                MaruVideoEditor()
            }
        }
    }
}
