package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlarmTone
import com.example.model.AppLanguage
import com.example.model.MapStyle
import com.example.model.UserLocation
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Side Navigation Drawer matching Screenshot 2:
 * - Brand: arreva [GPS] Alerte d'arrivée GPS
 * - Signal GPS capté + Détails
 * - Categories: NAVIGATION & TRAJETS, PRÉFÉRENCES
 * - Items: Moteur Cartographique, Sonneries, Favoris, Diagnostic & Signal GPS, Configuration AdMob, Activité en arrière-plan
 * - Footer: Sans compte ni inscription v1.2.0
 */
@Composable
fun NavigationMenuDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    selectedTone: AlarmTone,
    onToneChange: (AlarmTone) -> Unit,
    isTestingTone: Boolean,
    onTestToneToggle: () -> Unit,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    isSimulationMode: Boolean,
    onSimulationToggle: (Boolean) -> Unit,
    onOpenFavoritesManager: () -> Unit,
    onOpenTripHistory: () -> Unit = {},
    currentMapStyle: MapStyle = MapStyle.GOOGLE_MAPS,
    onToggleMapStyle: () -> Unit = {},
    userLocation: UserLocation? = null,
    isFavoriteAlertsEnabled: Boolean = true,
    onToggleFavoriteAlerts: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showGpsDetails by remember { mutableStateOf(false) }

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

    if (!isOpen) return

    // Full screen overlay with sliding left drawer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .background(ArrevaDarkTokens.NavySurface)
                .clickable(enabled = false) {} // Prevent dismiss when tapping drawer content
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // =====================================================================
                // HEADER: arreva [GPS] (Exact match to Screenshot 2)
                // =====================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Glowing Compass Icon in Orange/Amber Disc
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(8.dp, CircleShape, spotColor = ArrevaDarkTokens.AmberLight)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            ArrevaDarkTokens.AmberGlow,
                                            ArrevaDarkTokens.AmberLight,
                                            ArrevaDarkTokens.AmberPrimary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "arreva",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArrevaDarkTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ArrevaDarkTokens.AmberPrimary.copy(alpha = 0.35f))
                                        .border(BorderStroke(1.dp, ArrevaDarkTokens.AmberLight), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GPS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArrevaDarkTokens.AmberGlow
                                    )
                                }
                            }
                            Text(
                                text = "Alerte d'arrivée GPS",
                                fontSize = 13.sp,
                                color = ArrevaDarkTokens.TextSecondary
                            )
                        }
                    }

                    // Circular Close Button [✕]
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ArrevaDarkTokens.NavyCard)
                            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("drawer_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArrevaDarkTokens.TextLightSlate,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =====================================================================
                // GPS STATUS ROW: ((·)) Signal GPS capté + Détails
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArrevaDarkTokens.NavyCard)
                        .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = ArrevaDarkTokens.EmeraldGps,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Signal GPS capté",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.TextPrimary
                        )
                    }

                    Text(
                        text = "Détails",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.AmberLight,
                        modifier = Modifier.clickable { showGpsDetails = !showGpsDetails }
                    )
                }

                if (showGpsDetails && userLocation != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ArrevaDarkTokens.NavyBackground)
                            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Lat: ${String.format("%.5f", userLocation.latitude)} | Lng: ${String.format("%.5f", userLocation.longitude)}\nPrécision: ±${userLocation.accuracyMeters.toInt()}m | Vitesse: ${userLocation.speedKmh.toInt()} km/h",
                            fontSize = 11.sp,
                            color = ArrevaDarkTokens.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // =====================================================================
                // SECTION: NAVIGATION & TRAJETS (Exact match to Screenshot 2)
                // =====================================================================
                Text(
                    text = "NAVIGATION & TRAJETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArrevaDarkTokens.TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 1: Moteur Cartographique
                DrawerMenuItem(
                    icon = Icons.Default.Map,
                    iconBg = ArrevaDarkTokens.IconBgBlue,
                    iconTint = Color(0xFF60A5FA),
                    title = "Moteur Cartographique",
                    subtitle = if (currentMapStyle == MapStyle.OPENSTREETMAP) "OpenStreetMap Platform" else "Google Maps Platform",
                    badgeText = if (currentMapStyle == MapStyle.OPENSTREETMAP) "OSM" else "Google",
                    onClick = onToggleMapStyle
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 2: Sonneries
                DrawerMenuItem(
                    icon = Icons.Default.MusicNote,
                    iconBg = ArrevaDarkTokens.IconBgAmber,
                    iconTint = ArrevaDarkTokens.AmberGlow,
                    title = "Sonneries",
                    subtitle = "Sons de réveil, volume et vibrations",
                    badgeText = when (selectedTone) {
                        AlarmTone.SIREN -> "Sirène"
                        AlarmTone.URGENT -> "Bips"
                        AlarmTone.SOFT -> "Doux"
                    },
                    onClick = onTestToneToggle
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 3: Favoris
                DrawerMenuItem(
                    icon = Icons.Default.Star,
                    iconBg = ArrevaDarkTokens.IconBgGold,
                    iconTint = Color(0xFFFACC15),
                    title = "Favoris",
                    subtitle = if (isFavoriteAlertsEnabled) "Gares & arrêts • Alerte d'approche active" else "Gares, métros et arrêts enregistrés",
                    badgeText = if (isFavoriteAlertsEnabled) "Alerte ON ✓" else "OFF",
                    onClick = {
                        onDismiss()
                        onOpenFavoritesManager()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item: Historique des Trajets
                DrawerMenuItem(
                    icon = Icons.Default.History,
                    iconBg = Color(0xFF1E293B),
                    iconTint = ArrevaDarkTokens.AmberGlow,
                    title = "Historique des Trajets",
                    subtitle = "Derniers trajets et arrêts validés",
                    onClick = {
                        onDismiss()
                        onOpenTripHistory()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 4: Diagnostic & Signal GPS
                DrawerMenuItem(
                    icon = Icons.Default.Explore,
                    iconBg = ArrevaDarkTokens.IconBgTeal,
                    iconTint = ArrevaDarkTokens.EmeraldGps,
                    title = "Diagnostic & Signal GPS",
                    subtitle = "Précision en mètres, vitesse et coordonnées",
                    onClick = { showGpsDetails = !showGpsDetails }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 5: Configuration AdMob / Publicité
                DrawerMenuItem(
                    icon = Icons.Default.Campaign,
                    iconBg = ArrevaDarkTokens.IconBgPurple,
                    iconTint = Color(0xFFC084FC),
                    title = "Configuration AdMob / Publicité",
                    subtitle = "Unity Ads & Google AdMob SDK",
                    badgeText = "Actif"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 6: Activité en arrière-plan (User Request: Autoriser l'activité en arrière-plan)
                DrawerMenuItem(
                    icon = Icons.Default.ElectricBolt,
                    iconBg = ArrevaDarkTokens.IconBgEmerald,
                    iconTint = ArrevaDarkTokens.EmeraldGps,
                    title = "Activité en arrière-plan",
                    subtitle = if (isBackgroundAllowed) "Autorisé (sans restriction de batterie)" else "Appuyer pour autoriser en arrière-plan",
                    badgeText = if (isBackgroundAllowed) "Actif ✓" else "Configurer",
                    onClick = { requestBackgroundActivity(context) }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // =====================================================================
                // SECTION: PRÉFÉRENCES & FOOTER (Exact match to Screenshot 2)
                // =====================================================================
                Text(
                    text = "PRÉFÉRENCES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArrevaDarkTokens.TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Box: Sans compte ni inscription v1.2.0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ArrevaDarkTokens.NavyCard)
                        .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ArrevaDarkTokens.EmeraldGps,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sans compte ni inscription",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArrevaDarkTokens.TextPrimary
                                )
                            }
                            Text(
                                text = "v1.2.0",
                                fontSize = 12.sp,
                                color = ArrevaDarkTokens.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Navigation GPS autonome adaptée aux transports en commun, trains, bus et métros.",
                            fontSize = 12.sp,
                            color = ArrevaDarkTokens.TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Square Icon Container
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ArrevaDarkTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ArrevaDarkTokens.TextSecondary
            )
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArrevaDarkTokens.TextLightSlate
                )
            }
        }
    }
}
