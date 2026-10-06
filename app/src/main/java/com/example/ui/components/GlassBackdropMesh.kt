package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Frutiger Aero Ambient Backdrop Mesh
 * Fuses nature imagery (vibrant blue sky, water droplets, sun rays, green grass)
 * with optimistic 2000s technology and translucent aqua bubbles.
 */
@Composable
fun GlassBackdropMesh(
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f
) {
    FrutigerAeroBackdrop(
        modifier = modifier,
        alpha = alpha
    )
}
