package com.matanboas.maslul.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightBlue = Color(0xFF1E88E5)

// Custom theme composable to set light blue as the primary color
@Composable
fun MaslulTheme(content: @Composable () -> Unit) {
    // Check if the system is in dark mode
    val isDarkTheme = isSystemInDarkTheme()
    // Select the color scheme based on the system theme, overriding primary color
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = LightBlue
        )
    } else {
        lightColorScheme(
            primary = LightBlue
        )
    }
    // Apply the MaterialTheme with the custom color scheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}