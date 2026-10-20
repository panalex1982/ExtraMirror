package com.bue.extramirror.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Gauge orange from the legacy speedometer (rgb 255, 178, 102). */
val GaugeOrange = Color(0xFFFFB266)
val GaugeOrangeLight = Color(0xFFFFCC99)
val LawnGreen = Color(0xFF7CFC00)

// A dark scheme only: the app is used in a car, often at night, over a camera picture.
private val ExtraMirrorColors = darkColorScheme(
    primary = GaugeOrange,
    onPrimary = Color.Black,
    secondary = LawnGreen,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1B1B1B),
    onSurface = Color.White,
)

@Composable
fun ExtraMirrorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ExtraMirrorColors, content = content)
}
