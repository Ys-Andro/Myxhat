package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurpleLight,
    onPrimary = Color.Black,
    primaryContainer = NeonPurple,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = DeepSurfaceVariant,
    onSecondaryContainer = NeonCyan,
    tertiary = WarmRoseLight,
    onTertiary = Color.Black,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = DeepSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = NeonPurple,
    secondary = NeonCyanDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7F6),
    onSecondaryContainer = Color(0xFF007568),
    tertiary = WarmRose,
    onTertiary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A24),
    surface = Color.White,
    onSurface = Color(0xFF1A1A24),
    surfaceVariant = Color(0xFFEDEEF3),
    onSurfaceVariant = Color(0xFF4A4B57),
    outline = Color(0xFFB0B2C3),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode for futuristic AI feel
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
