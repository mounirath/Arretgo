package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.FrutigerAqua
import com.example.ui.theme.FrutigerGrassGreen
import com.example.ui.theme.FrutigerSkyBlue
import com.example.ui.theme.FrutigerSunbeamYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Frutiger Aero Ambient Backdrop
 * Fuses nature imagery (vibrant blue sky, water droplets, sun rays, green grass)
 * with optimistic 2000s technology:
 * - High-res nature backdrop
 * - Layered sky-to-grass luminous gradient overlays
 * - Floating translucent glass bubbles with curved specular crescent highlights
 * - Bokeh discs, sunbeam caustics, and diagonal sheen sweeps
 */
@Composable
fun FrutigerAeroBackdrop(
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f,
    reduceMotion: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "frutiger_aero_ambient")

    // Sunbeam and Caustic shimmer
    val sunbeamPulse by if (reduceMotion) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "sunbeam_static"
        )
    } else {
        transition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(4500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sunbeam_pulse"
        )
    }

    // Floating Glass Bubbles Y-offset animation
    val bubbleFloat by if (reduceMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "bubble_static"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(9000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bubble_float"
        )
    }

    // Diagonal Sheen Sweep
    val sheenProgress by if (reduceMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "sheen_static"
        )
    } else {
        transition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sheen_sweep"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Photorealistic Nature Imagery (Blue sky, green grass meadow, water droplets)
        Image(
            painter = painterResource(id = R.drawable.img_frutiger_aero_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = alpha
        )

        // Layer 2: Luminous Sky-to-Meadow Gradient Filter (Vivid white light, sky blue to grass green)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to FrutigerSkyBlue.copy(alpha = 0.22f),
                        0.35f to Color.White.copy(alpha = 0.15f),
                        0.70f to FrutigerAqua.copy(alpha = 0.18f),
                        1.0f to FrutigerGrassGreen.copy(alpha = 0.30f)
                    )
                )
        )

        // Layer 3: Canvas for Floating Translucent Bubbles, Sunbeams & Bokeh Circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Sun Rays from top-left corner
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        FrutigerSunbeamYellow.copy(alpha = 0.35f),
                        FrutigerAqua.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.08f),
                    radius = w * 0.85f * sunbeamPulse
                ),
                center = Offset(w * 0.15f, h * 0.08f),
                radius = w * 0.85f * sunbeamPulse
            )

            // 2. Bokeh Circles (Diffused white and cyan light discs)
            drawBokehCircle(Offset(w * 0.75f, h * 0.25f), 38f, 0.40f)
            drawBokehCircle(Offset(w * 0.88f, h * 0.38f), 24f, 0.35f)
            drawBokehCircle(Offset(w * 0.22f, h * 0.62f), 45f, 0.25f)
            drawBokehCircle(Offset(w * 0.65f, h * 0.78f), 32f, 0.30f)
            drawBokehCircle(Offset(w * 0.12f, h * 0.32f), 28f, 0.30f)

            // 3. Floating Translucent Glass Bubbles (with curved specular crescent shine)
            val bubbleList = listOf(
                Triple(0.25f, 0.40f, 36f),
                Triple(0.80f, 0.55f, 48f),
                Triple(0.45f, 0.75f, 28f),
                Triple(0.70f, 0.20f, 22f),
                Triple(0.15f, 0.82f, 42f),
                Triple(0.85f, 0.85f, 32f)
            )

            bubbleList.forEach { (relX, baseRelY, radius) ->
                // Apply subtle vertical float motion
                val animatedY = ((baseRelY - bubbleFloat * 0.25f) % 1.0f + 1.0f) % 1.0f
                drawAeroBubble(
                    center = Offset(w * relX, h * animatedY),
                    radius = radius
                )
            }

            // 4. Diagonal Sheen Sweep (Optic Glass Flash)
            val sheenX = w * sheenProgress
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 120f, 0f),
                    end = Offset(sheenX + 120f, h)
                ),
                start = Offset(sheenX - 120f, 0f),
                end = Offset(sheenX + 120f, h),
                strokeWidth = 60f
            )
        }
    }
}

private fun DrawScope.drawBokehCircle(center: Offset, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                FrutigerAqua.copy(alpha = alpha * 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

/**
 * Draws an authentic 2000s Frutiger Aero Aqua Bubble:
 * - Translucent aqua tinted body
 * - Sharp white specular curved highlight (top-left crescent)
 * - Soft secondary caustic reflection (bottom-right)
 */
private fun DrawScope.drawAeroBubble(center: Offset, radius: Float) {
    // 1. Translucent bubble sphere body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                FrutigerAqua.copy(alpha = 0.25f),
                Color(0xFF38BDF8).copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.60f) // Specular rim
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // 2. Primary Curved Specular Crescent Highlight (Top-Left)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.38f),
            radius = radius * 0.45f
        ),
        radius = radius * 0.45f,
        center = Offset(center.x - radius * 0.35f, center.y - radius * 0.38f)
    )

    // 3. Secondary Caustic Rim Reflection (Bottom-Right)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                FrutigerGrassGreen.copy(alpha = 0.30f),
                FrutigerAqua.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = Offset(center.x + radius * 0.30f, center.y + radius * 0.32f),
            radius = radius * 0.35f
        ),
        radius = radius * 0.35f,
        center = Offset(center.x + radius * 0.30f, center.y + radius * 0.32f)
    )
}
