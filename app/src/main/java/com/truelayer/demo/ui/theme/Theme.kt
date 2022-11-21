package com.truelayer.demo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    surfaceVariant = SurfaceVariant
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryDark,
    secondary = Secondary,
    surfaceVariant = SurfaceVariant
)

@Composable
fun SDKDemoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
