package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Frutiger Aero Aqua Glass & Specular Gloss Tokens
 * Recreates the iconic 2000s glossy, luminous, translucent aero aesthetic:
 * - Sky-blue & water caustics with grass-green reflection
 * - Curved specular highlights and glossy caps
 * - Pure white specular edge shine (1.5px)
 * - Ultra-high contrast text backing
 */
object GlassTokens {
    // Frutiger Aero Translucent Aqua Glass Panels (Luminous & Specular)
    val GlassSurfaceTop = Color(0xD9FFFFFF)
    val GlassSurfaceBottom = Color(0xC7E0F2FE)
    val GlassCardTop = Color(0xF2FFFFFF)
    val GlassCardBottom = Color(0xDDF0FDF4)

    // Darker Aqua mode for high contrast
    val GlassDeepTop = Color(0xE60A2540)
    val GlassDeepBottom = Color(0xEB0E3B66)

    // Specular Curved Borders (Brilliant white light at top angle, aquatic cyan at bottom)
    val GlassBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.95f),
            Color(0xFFBAE6FD).copy(alpha = 0.60f),
            Color(0xFF86EFAC).copy(alpha = 0.45f)
        )
    )

    val GlassCardBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.98f),
            Color(0xFF38BDF8).copy(alpha = 0.50f),
            Color.White.copy(alpha = 0.30f)
        )
    )

    val GlassAccentBorderBrush = Brush.linearGradient(
        listOf(
            Color(0xFF00E5FF).copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.95f),
            Color(0xFF22C55E).copy(alpha = 0.70f)
        )
    )

    // Gloss Cap Brush (Top half specular shine for buttons and cards)
    val GlossCapBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.75f),
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    // Ambient Orbs for Backdrop
    val BackdropSky = Color(0xFF0099FF)
    val BackdropGrass = Color(0xFF22C55E)
    val BackdropAqua = Color(0xFF00E5FF)
    val BackdropSun = Color(0xFFFACC15)
    val BackdropPurple = Color(0xFF818CF8)
    val BackdropBlue = Color(0xFF38BDF8)
    val BackdropPink = Color(0xFFF43F5E)
}

/**
 * Frutiger Aero Glossy Glass Surface Component
 */
@Composable
fun FrutigerAeroGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color(0xFF0284C7).copy(alpha = 0.35f),
                ambientColor = Color.White.copy(alpha = 0.7f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        GlassTokens.GlassSurfaceTop,
                        GlassTokens.GlassSurfaceBottom
                    )
                )
            )
            .border(
                BorderStroke(1.5.dp, GlassTokens.GlassBorderBrush),
                RoundedCornerShape(cornerRadius)
            )
    ) {
        // Specular Top Gloss Sheen Overlay (Iconic Aero curved highlight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(GlassTokens.GlossCapBrush)
        )
        content()
    }
}
