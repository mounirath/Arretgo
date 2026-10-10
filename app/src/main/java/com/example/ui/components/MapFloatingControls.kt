package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.MapStyle
import com.example.ui.theme.ArrevaDarkTokens

@Composable
fun MapFloatingControls(
    onCenterLocation: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    currentMapStyle: MapStyle,
    onMapStyleChange: (MapStyle) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    var isLayersMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Layers / Map Style (Exact icon stack button seen in Screenshot 1)
        Box {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                    .clickable { isLayersMenuOpen = true }
                    .testTag("btn_map_layers"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Map Style Layers",
                    tint = ArrevaDarkTokens.TextLightSlate,
                    modifier = Modifier.size(22.dp)
                )
            }

            DropdownMenu(
                expanded = isLayersMenuOpen,
                onDismissRequest = { isLayersMenuOpen = false },
                modifier = Modifier
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(14.dp))
            ) {
                MapStyle.values().forEach { style ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (style == currentMapStyle) {
                                    Text(
                                        text = "✓ ",
                                        color = ArrevaDarkTokens.AmberLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> style.labelAr
                                        AppLanguage.EN -> style.labelEn
                                        AppLanguage.FR -> style.labelFr
                                    },
                                    color = if (style == currentMapStyle) ArrevaDarkTokens.AmberLight else ArrevaDarkTokens.TextPrimary,
                                    fontWeight = if (style == currentMapStyle) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = {
                            onMapStyleChange(style)
                            isLayersMenuOpen = false
                        }
                    )
                }
            }
        }

        // 2. Center on GPS Location Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(ArrevaDarkTokens.NavyCard)
                .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                .clickable(onClick = onCenterLocation)
                .testTag("btn_center_location"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Center on Me",
                tint = ArrevaDarkTokens.EmeraldGps,
                modifier = Modifier.size(22.dp)
            )
        }

        // 3. Fullscreen / Focus Mode Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(ArrevaDarkTokens.NavyCard)
                .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                .clickable(onClick = onToggleFullscreen)
                .testTag("btn_toggle_fullscreen"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Toggle full map view",
                tint = ArrevaDarkTokens.TextLightSlate,
                modifier = Modifier.size(22.dp)
            )
        }

        // 4. Zoom In Button (+)
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(ArrevaDarkTokens.NavyCard)
                .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                .clickable(onClick = onZoomIn)
                .testTag("btn_zoom_in"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom in",
                tint = ArrevaDarkTokens.TextLightSlate,
                modifier = Modifier.size(20.dp)
            )
        }

        // 5. Zoom Out Button (-)
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(ArrevaDarkTokens.NavyCard)
                .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                .clickable(onClick = onZoomOut)
                .testTag("btn_zoom_out"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom out",
                tint = ArrevaDarkTokens.TextLightSlate,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
