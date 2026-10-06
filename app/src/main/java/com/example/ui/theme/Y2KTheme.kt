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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Y2K Digital Aesthetic Tokens & Components
 *
 * Defining Signals:
 * - Liquid-chrome & metallic mirror surfaces (multi-stop silver highlights)
 * - Glossy translucent gel buttons with strong specular top gloss oval caps
 * - Iridescent electric-blue / silver / white palette with holographic cyan-magenta shifts
 * - Wide techno display type (Eurostile-flavored, italic, bold, wide tracking)
 * - Synthetic optimism: No grass, no water, no sky; pure cybernetic & metallic precision
 * - Monospace pixel-font tech tags & telemetry readouts
 */
object Y2KTokens {

    // Cyber Obsidians & Metallic Backings
    val ObsidianVoid = Color(0xFF030712)
    val CyberChromeDark = Color(0xFF0F172A)
    val MetallicGraphite = Color(0xFF1E293B)
    val SteelBacking = Color(0xFF334155)

    // Liquid Chrome Mirror Gradients (Alternating specular light & shadow metallic stops)
    val LiquidChromeBrush = Brush.linearGradient(
        listOf(
            Color(0xFFFFFFFF), // pure mirror flash
            Color(0xFFCBD5E1), // light silver
            Color(0xFF64748B), // deep specular shadow
            Color(0xFFF8FAFC), // high mirror sheen
            Color(0xFF94A3B8), // chrome midtone
            Color(0xFFFFFFFF)  // edge rim light
        )
    )

    val ChromeBorderBrush = Brush.linearGradient(
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFF38BDF8), // electric blue iridescent shift
            Color(0xFFE2E8F0),
            Color(0xFFD946EF), // holographic magenta iridescent shift
            Color(0xFFFFFFFF)
        )
    )

    // Holographic Cyan-Magenta Gel Gradients
    val HolographicGelBrush = Brush.linearGradient(
        listOf(
            Color(0xFF00F0FF), // Cyber Cyan
            Color(0xFF3B82F6), // Electric Blue
            Color(0xFFD946EF)  // Holographic Magenta
        )
    )

    val ElectricBlueGelBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF60A5FA),
            Color(0xFF2563EB),
            Color(0xFF1D4ED8)
        )
    )

    // Specular Top Gloss Cap for Gel Surfaces
    val SpecularGlossCap = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.35f),
            Color.Transparent
        )
    )

    // Text & Telemetry Colors
    val TextPureWhite = Color(0xFFFFFFFF)
    val TextSilver = Color(0xFFE2E8F0)
    val TextCyanGlow = Color(0xFF00F0FF)
    val TextMagentaPulse = Color(0xFFF43F5E)
    val TextMutedSteel = Color(0xFF94A3B8)
}

/**
 * Y2K Liquid Chrome Capsule Surface
 * Replaces generic frosted glass with high-sheen liquid chrome metallic framing.
 */
@Composable
fun Y2KChromeSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    elevation: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = Color(0xFF00F0FF).copy(alpha = 0.30f),
                ambientColor = Color.White.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(Y2KTokens.CyberChromeDark.copy(alpha = 0.88f))
            .border(BorderStroke(1.5.dp, Y2KTokens.ChromeBorderBrush), shape)
    ) {
        // Specular Mirror Top Sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.TopCenter)
                .background(Y2KTokens.SpecularGlossCap)
        )

        content()
    }
}

/**
 * Y2K Glossy Gel Button
 * Interactive pill/circle with deep gel refraction and high-impact specular highlight.
 */
@Composable
fun Y2KGelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Y2KTokens.LiquidChromeBrush,
    shape: Shape = CircleShape,
    contentDescription: String? = null,
    testTag: String = "y2k_btn",
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "y2k_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 10.dp,
                shape = shape,
                spotColor = Color(0xFF00F0FF).copy(alpha = 0.40f),
                ambientColor = Color.White
            )
            .clip(shape)
            .background(brush)
            .border(BorderStroke(1.5.dp, Y2KTokens.ChromeBorderBrush), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Upper Specular Reflection Cap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .align(Alignment.TopCenter)
                .background(Y2KTokens.SpecularGlossCap)
        )

        content()
    }
}

/**
 * Y2K Wide Techno Display Typography (Eurostile-flavored italic bold)
 */
@Composable
fun Y2KTechnoText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = Y2KTokens.TextPureWhite,
    letterSpacing: TextUnit = 2.sp
) {
    Text(
        text = text,
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Black,
        letterSpacing = letterSpacing,
        color = color,
        fontSize = fontSize,
        modifier = modifier
    )
}

/**
 * Y2K Monospace Telemetry Cyber Tag
 */
@Composable
fun Y2KTelemetryTag(
    text: String,
    modifier: Modifier = Modifier,
    tagColor: Color = Y2KTokens.TextCyanGlow
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Y2KTokens.ObsidianVoid.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, tagColor.copy(alpha = 0.60f)), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = tagColor
        )
    }
}
