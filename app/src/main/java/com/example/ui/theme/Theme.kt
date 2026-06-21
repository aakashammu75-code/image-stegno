package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberDarkBg,
    secondary = CyberIndigo,
    onSecondary = TextPrimary,
    tertiary = CyberGlowGreen,
    background = CyberDarkBg,
    onBackground = TextPrimary,
    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = CyberWarningPink
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
