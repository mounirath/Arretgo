package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoritePlace
import com.example.model.AppLanguage
import com.example.model.LocationPoint
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Sliding Bottom Panel matching Screenshot 1 exactly:
 * - Header: • En attente de départ / Fermer ⌵ in amber
 * - Title row: "En attente de départ" + 4 quick square action buttons [🎵] [⭐] [🔊] [🎛]
 * - Card: "Touchez la carte ou cherchez une adresse pour définir votre arrêt d'arrivée"
 * - Primary CTA: Amber/Gold button [▶ Démarrer le suivi du trajet]
 * - Bottom links: "Simuler un trajet (démo)", "📱 Écran maintenu allumé", "⚡ Activité en arrière-plan autorisée"
 */
@Composable
fun GoogleMapsBottomSheet(
    destination: LocationPoint?,
    alertRadiusMeters: Int,
    onRadiusChange: (Int) -> Unit,
    userDistanceMeters: Float,
    isTripActive: Boolean,
    onStartTrip: () -> Unit,
    onStopTrip: () -> Unit,
    onClearDestination: () -> Unit,
    onFocusDestinationOnMap: () -> Unit,
    onSaveToFavorites: () -> Unit,
    favorites: List<FavoritePlace>,
    onSelectFavorite: (FavoritePlace) -> Unit,
    onOpenFavoritesManager: () -> Unit,
    onOpenTripHistory: () -> Unit = {},
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    currentLanguage: AppLanguage,
    onTestToneToggle: () -> Unit = {},
    isSimulationMode: Boolean = false,
    onSimulationToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMenuOpen by remember { mutableStateOf(true) }
    var isKeepScreenOn by remember { mutableStateOf(true) }
    var showRadiusSettings by remember { mutableStateOf(false) }

    fun isIgnoringBattery(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else true
    }

    var isBackgroundAllowed by remember { mutableStateOf(isIgnoringBattery(context)) }

    fun requestBackgroundActivity(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                isBackgroundAllowed = true
            } catch (e: Exception) {
                try {
                    val appSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(appSettings)
                } catch (_: Exception) {}
            }
        }
    }

    val animatedHeight by animateDpAsState(
        targetValue = if (isMenuOpen) (if (showRadiusSettings) 520.dp else 420.dp) else 64.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sheet_height"
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 0f else 180f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "chevron_rot"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(ArrevaDarkTokens.NavySheet)
            .border(
                BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Drag handle at top center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMenuOpen = !isMenuOpen }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ArrevaDarkTokens.NavyBorder)
                )
            }

            // =========================================================================
            // SUB-HEADER: • En attente de départ | Fermer ⌵ (Screenshot 1)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTripActive) ArrevaDarkTokens.EmeraldGps
                                else if (destination != null) ArrevaDarkTokens.AmberLight
                                else ArrevaDarkTokens.TextTertiary
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTripActive) "Trajet en cours"
                        else if (destination != null) "Destination prête"
                        else "En attente de départ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArrevaDarkTokens.TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isMenuOpen = !isMenuOpen }
                ) {
                    Text(
                        text = if (isMenuOpen) "Fermer" else "Ouvrir",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.AmberLight
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (isMenuOpen) "⌵" else "▴",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.AmberLight
                    )
                }
            }

            if (isMenuOpen) {
                Spacer(modifier = Modifier.height(10.dp))

                // =====================================================================
                // TITLE ROW: "En attente de départ" + 4 Quick Square Action Buttons
                // =====================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = destination?.name ?: "En attente de départ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 4 Action Buttons [🎵] [⭐] [🔊] [🎛]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. [🎵] Music / Sonnerie
                        SquareActionButton(
                            icon = Icons.Default.MusicNote,
                            tint = Color(0xFF60A5FA),
                            onClick = onTestToneToggle
                        )

                        // 2. [⭐] Favoris
                        SquareActionButton(
                            icon = Icons.Default.Star,
                            tint = Color(0xFFFACC15),
                            onClick = {
                                if (destination != null) onSaveToFavorites()
                                else onOpenFavoritesManager()
                            }
                        )

                        // 3. [🔊] Volume
                        SquareActionButton(
                            icon = Icons.Default.VolumeUp,
                            tint = Color(0xFF38BDF8),
                            onClick = onTestToneToggle
                        )

                        // 4. [🎛] Paramètres / Rayon
                        SquareActionButton(
                            icon = Icons.Default.Settings,
                            tint = ArrevaDarkTokens.TextLightSlate,
                            onClick = { showRadiusSettings = !showRadiusSettings }
                        )

                        // 5. [🕒] Historique des trajets
                        SquareActionButton(
                            icon = Icons.Default.History,
                            tint = ArrevaDarkTokens.AmberGlow,
                            onClick = onOpenTripHistory
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =====================================================================
                // INSTRUCTION / STATUS CARD (Screenshot 1)
                // =====================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ArrevaDarkTokens.NavyCard)
                        .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (destination != null) {
                            "Destination : ${destination.name}\n${if (destination.address.isNotBlank()) destination.address else ""}"
                        } else {
                            "Touchez la carte ou cherchez une adresse pour définir votre arrêt d'arrivée"
                        },
                        fontSize = 13.sp,
                        color = ArrevaDarkTokens.TextLightSlate,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                // Radius settings slider if toggled
                if (showRadiusSettings) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ArrevaDarkTokens.NavyBackground)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rayon de réveil anticipé",
                                fontSize = 12.sp,
                                color = ArrevaDarkTokens.TextSecondary
                            )
                            Text(
                                text = formatDistance(alertRadiusMeters),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.AmberLight
                            )
                        }
                        Slider(
                            value = alertRadiusMeters.toFloat(),
                            onValueChange = { onRadiusChange(it.toInt()) },
                            valueRange = 100f..5000f,
                            colors = SliderDefaults.colors(
                                thumbColor = ArrevaDarkTokens.AmberLight,
                                activeTrackColor = ArrevaDarkTokens.AmberPrimary,
                                inactiveTrackColor = ArrevaDarkTokens.NavyBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =====================================================================
                // MAIN CTA BUTTON: Solid Amber Button (Screenshot 1)
                // =====================================================================
                Button(
                    onClick = {
                        if (isTripActive) onStopTrip() else onStartTrip()
                    },
                    enabled = destination != null || isTripActive,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTripActive) Color(0xFFEF4444) else ArrevaDarkTokens.AmberPrimary,
                        disabledContainerColor = ArrevaDarkTokens.NavyCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_main_action")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTripActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (destination != null || isTripActive) ArrevaDarkTokens.TextOnAmber else ArrevaDarkTokens.TextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTripActive) {
                                "Arrêter le suivi du trajet"
                            } else {
                                "Démarrer le suivi du trajet"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (destination != null || isTripActive) ArrevaDarkTokens.TextOnAmber else ArrevaDarkTokens.TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =====================================================================
                // BOTTOM SECONDARY OPTIONS ROW (Screenshot 1)
                // =====================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Simuler un trajet (démo) (underlined link)
                    Text(
                        text = if (isSimulationMode) "Mode réel" else "Simuler un trajet (démo)",
                        fontSize = 12.sp,
                        color = ArrevaDarkTokens.TextSecondary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onSimulationToggle(!isSimulationMode) }
                    )

                    // 2. Écran maintenu allumé
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isKeepScreenOn = !isKeepScreenOn }
                    ) {
                        Text(
                            text = "📱",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Écran maintenu allumé",
                            fontSize = 12.sp,
                            color = if (isKeepScreenOn) ArrevaDarkTokens.TextLightSlate else ArrevaDarkTokens.TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Activité en arrière-plan (User Request: Autoriser l'activité en arrière-plan)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArrevaDarkTokens.NavyCard)
                        .clickable { requestBackgroundActivity(context) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = if (isBackgroundAllowed) ArrevaDarkTokens.EmeraldGps else ArrevaDarkTokens.AmberLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBackgroundAllowed) "Activité en arrière-plan autorisée" else "Autoriser l'activité en arrière-plan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isBackgroundAllowed) ArrevaDarkTokens.EmeraldGps else ArrevaDarkTokens.AmberLight
                        )
                    }

                    Text(
                        text = if (isBackgroundAllowed) "Actif ✓" else "Activer ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBackgroundAllowed) ArrevaDarkTokens.EmeraldGps else ArrevaDarkTokens.AmberLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AdMob Banner
                AdMobBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SquareActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ArrevaDarkTokens.NavyCard)
            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

fun formatDistance(meters: Int): String {
    return if (meters >= 1000) {
        if (meters % 1000 == 0) "${meters / 1000} km" else String.format("%.1f km", meters / 1000f)
    } else {
        "$meters m"
    }
}

fun formatDistance(meters: Float): String {
    if (meters == Float.MAX_VALUE || meters < 0f) return "--"
    return formatDistance(meters.toInt())
}
