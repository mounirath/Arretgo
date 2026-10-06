package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple Liquid Glass Material Tokens & Components
 *
 * Core Principles:
 * 1. Glass is reserved strictly for the floating control layer (toolbars, buttons, capsules)
 *    above unrestricted opaque content.
 * 2. Lenses what's beneath — blur simulation with chromatic refraction-like edge highlights,
 *    caustic inner bevels, and specular light catching, never flat dead frost.
 * 3. Self-adapting tint: Automatically shifts luminance and contrast between light terrain
 *    and dark terrain (e.g. satellite/night modes) for uninterrupted legibility.
 * 4. Concentric capsule shapes with continuous curvature (iOS Squircle / Capsule geometry).
 * 5. High-contrast & reduced transparency accessibility compliance.
 */
object LiquidGlassTokens {

    // Light Terrain Liquid Glass (Luminous refraction, crisp specular reflection)
    val LightGlassBase = Color(0xCCFFFFFF) // 80% opacity translucent core
    val LightGlassSubstrate = Color(0xB8E8F2FC) // subtle chromatic refraction tint
    val LightTextPrimary = Color(0xFF0F172A)
    val LightTextSecondary = Color(0xFF475569)
    val LightIconTint = Color(0xFF0284C7)

    // Dark Terrain Liquid Glass (Dark crystalline refraction over Satellite / Dark mode)
    val DarkGlassBase = Color(0xB80B132B) // 72% opacity deep refractive crystalline
    val DarkGlassSubstrate = Color(0x991C2541)
    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFF94A3B8)
    val DarkIconTint = Color(0xFF38BDF8)

    // Refractive Edge Bevel Brushes (Simulates physical light refracting through curved glass meniscus)
    val LightRefractionBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.95f),   // Specular top highlight
            Color(0x80BAE6FD),                 // Chromatic cyan refraction mid-body
            Color(0x3038BDF8),                 // Caustic transition
            Color.White.copy(alpha = 0.40f)    // Bottom edge ground reflection
        )
    )

    val DarkRefractionBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.85f),
            Color(0x6038BDF8),
            Color(0x200284C7),
            Color.White.copy(alpha = 0.20f)
        )
    )

    // Meniscus Lens Gloss Cap (Refraction curve catching overhead ambient skylight)
    val MeniscusGlossBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.22f),
            Color.Transparent
        )
    )

    val DarkMeniscusGlossBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.12f),
            Color.Transparent
        )
    )

    // Inner Caustic Edge Glow (simulates internal total reflection within thick glass)
    val LightInnerCaustic = Brush.radialGradient(
        listOf(
            Color(0x2000E5FF),
            Color.Transparent
        )
    )
}

/**
 * Liquid Glass Capsule Surface - Floating Control Element
 * Used for floating search bars, HUD pill bars, tab bars, and control clusters.
 */
@Composable
fun LiquidGlassCapsule(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    isDarkTerrain: Boolean = false,
    elevation: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val baseColor = if (isDarkTerrain) LiquidGlassTokens.DarkGlassBase else LiquidGlassTokens.LightGlassBase
    val substrateColor = if (isDarkTerrain) LiquidGlassTokens.DarkGlassSubstrate else LiquidGlassTokens.LightGlassSubstrate
    val borderBrush = if (isDarkTerrain) LiquidGlassTokens.DarkRefractionBorder else LiquidGlassTokens.LightRefractionBorder
    val glossBrush = if (isDarkTerrain) LiquidGlassTokens.DarkMeniscusGlossBrush else LiquidGlassTokens.MeniscusGlossBrush
    val spotColor = if (isDarkTerrain) Color.Black.copy(alpha = 0.60f) else Color(0xFF0369A1).copy(alpha = 0.28f)

    Box(
        modifier = modifier
            // Optical elevation drop shadow: soft ambient occlusion + focused caustic rim
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = spotColor,
                ambientColor = Color.White.copy(alpha = if (isDarkTerrain) 0.1f else 0.5f)
            )
            .clip(shape)
            // Lensed substrate gradient (blur simulation + chromatic base)
            .background(
                Brush.verticalGradient(
                    listOf(
                        baseColor,
                        substrateColor
                    )
                )
            )
            // Physical glass refraction border
            .border(
                BorderStroke(1.25.dp, borderBrush),
                shape
            )
    ) {
        // Specular Meniscus Cap (upper-half refraction line)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .align(Alignment.TopCenter)
                .background(glossBrush)
        )

        content()
    }
}

/**
 * Liquid Glass Circular Button - Precision Floating Control
 * Concentric capsule geometry with tactile spring response and refractive highlight.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTerrain: Boolean = false,
    contentDescription: String? = null,
    testTag: String = "liquid_glass_btn",
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "glass_button_scale"
    )

    val baseColor = if (isDarkTerrain) {
        if (isPressed) Color(0xD91E293B) else LiquidGlassTokens.DarkGlassBase
    } else {
        if (isPressed) Color(0xE6F1F5F9) else LiquidGlassTokens.LightGlassBase
    }

    val borderBrush = if (isDarkTerrain) LiquidGlassTokens.DarkRefractionBorder else LiquidGlassTokens.LightRefractionBorder
    val glossBrush = if (isDarkTerrain) LiquidGlassTokens.DarkMeniscusGlossBrush else LiquidGlassTokens.MeniscusGlossBrush
    val spotColor = if (isDarkTerrain) Color.Black.copy(alpha = 0.50f) else Color(0xFF0284C7).copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 10.dp,
                shape = CircleShape,
                spotColor = spotColor,
                ambientColor = Color.White.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(baseColor)
            .border(BorderStroke(1.25.dp, borderBrush), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Upper refraction gloss meniscus
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.TopCenter)
                .background(glossBrush)
        )

        content()
    }
}
