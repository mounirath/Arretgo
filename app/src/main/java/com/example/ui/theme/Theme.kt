package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =========================================================================
// FRUTIGER AERO MATERIAL 3 COLOR SCHEMES
// Luminous Sky-Blue, Meadow Grass-Green, Translucent Aqua Glass & High Contrast
// =========================================================================

private val FrutigerAeroLightColorScheme = lightColorScheme(
    primary = FrutigerSkyBlue,
    onPrimary = Color.White,
    primaryContainer = FrutigerSkyPale,
    onPrimaryContainer = FrutigerDeepNavy,
    secondary = FrutigerGrassGreen,
    onSecondary = Color.White,
    secondaryContainer = FrutigerLeafPale,
    onSecondaryContainer = FrutigerMeadowDark,
    tertiary = FrutigerAqua,
    onTertiary = FrutigerDeepNavy,
    tertiaryContainer = Color(0xFFE0F7FE),
    onTertiaryContainer = FrutigerAquaDeep,
    background = Color(0xFFF0F9FF),
    onBackground = FrutigerDeepNavy,
    surface = Color(0xF2FFFFFF),
    onSurface = FrutigerDeepNavy,
    surfaceVariant = Color(0xE6E0F2FE),
    onSurfaceVariant = FrutigerSlate,
    outline = Color(0xFF38BDF8).copy(alpha = 0.6f),
    error = FrutigerCoral
)

private val FrutigerAeroDarkColorScheme = darkColorScheme(
    primary = FrutigerSkyLight,
    onPrimary = FrutigerDeepNavy,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = FrutigerGrassLight,
    onSecondary = Color(0xFF052E16),
    secondaryContainer = Color(0xFF166534),
    onSecondaryContainer = Color(0xFFDCFCE7),
    tertiary = FrutigerAqua,
    onTertiary = FrutigerDeepNavy,
    background = FrutigerDeepNavy,
    onBackground = FrutigerTextWhite,
    surface = Color(0xE60F2B48),
    onSurface = FrutigerTextWhite,
    surfaceVariant = Color(0xE6163E65),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = FrutigerSkyLight.copy(alpha = 0.5f),
    error = FrutigerCoral
)

@Composable
fun ArrivaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Frutiger Aero focuses on luminous sky light & aquatic optimism
    val colorScheme = if (darkTheme) FrutigerAeroDarkColorScheme else FrutigerAeroLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
