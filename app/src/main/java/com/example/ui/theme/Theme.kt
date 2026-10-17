package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GymDarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = Color(0xFF0A0C0F),
    primaryContainer = Color(0xFF2C390B),
    onPrimaryContainer = ElectricLime,
    secondary = CyberEmerald,
    onSecondary = Color(0xFF002214),
    secondaryContainer = Color(0xFF0F3827),
    onSecondaryContainer = CyberEmerald,
    tertiary = NeonCyan,
    onTertiary = Color(0xFF002026),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Athletic dark gym theme by default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GymDarkColorScheme,
        typography = GymTypography,
        content = content
    )
}
