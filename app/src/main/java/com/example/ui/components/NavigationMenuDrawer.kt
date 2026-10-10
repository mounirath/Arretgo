package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ViewSidebar
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AlarmTone
import com.example.model.AppLanguage
import com.example.model.MapStyle
import com.example.model.UserLocation
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Side Navigation Drawer:
 * - Default compact mode: sleek vertical icon rail with clean labels and quick actions
 * - Expanded mode: full detailed menu with descriptions and settings
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
    onOpenToneSelection: () -> Unit = {},
    currentMapStyle: MapStyle = MapStyle.GOOGLE_MAPS,
    onToggleMapStyle: () -> Unit = {},
    userLocation: UserLocation? = null,
    isFavoriteAlertsEnabled: Boolean = true,
    onToggleFavoriteAlerts: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showGpsDetails by remember { mutableStateOf(false) }

    var isBackgroundAllowed by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) == true
            } else true
        )
    }

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

    // By default, open directly in icon-only mode as requested by user
    var isIconOnlyMode by remember { mutableStateOf(true) }
    val drawerWidth by animateDpAsState(
        targetValue = if (isIconOnlyMode) 78.dp else 295.dp,
        label = "drawerWidth"
    )

    // Full screen overlay with sliding left drawer / icon rail
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .background(ArrevaDarkTokens.NavySurface)
                .clickable(enabled = false) {} // Prevent dismiss when tapping inside drawer
        ) {
            if (isIconOnlyMode) {
                // =====================================================================
                // MODE COMPACT: BARRE D'ICÔNES (ICON-ONLY NAVIGATION RAIL)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Icon arreva
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            contentDescription = "arreva GPS",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "arreva",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = ArrevaDarkTokens.AmberLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button: Agrandir / Détails (switch to full text drawer)
                    IconRailButton(
                        icon = Icons.Default.MenuOpen,
                        iconBg = ArrevaDarkTokens.NavyCard,
                        iconTint = ArrevaDarkTokens.AmberLight,
                        label = "Détails",
                        testTag = "btn_toggle_drawer_expand",
                        onClick = { isIconOnlyMode = false }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Small divider
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(1.dp)
                            .background(ArrevaDarkTokens.NavyBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Item 1: Moteur Cartographique (Carte)
                    IconRailButton(
                        icon = Icons.Default.Map,
                        iconBg = ArrevaDarkTokens.IconBgBlue,
                        iconTint = Color(0xFF60A5FA),
                        label = "Carte",
                        badgeText = if (currentMapStyle == MapStyle.OPENSTREETMAP) "OSM" else "Google",
                        testTag = "rail_btn_map_style",
                        onClick = onToggleMapStyle
                    )

                    // Item 2: Sonneries
                    IconRailButton(
                        icon = Icons.Default.MusicNote,
                        iconBg = ArrevaDarkTokens.IconBgAmber,
                        iconTint = ArrevaDarkTokens.AmberGlow,
                        label = "Sons",
                        testTag = "rail_btn_tones",
                        onClick = {
                            onDismiss()
                            onOpenToneSelection()
                        }
                    )

                    // Item 3: Favoris
                    IconRailButton(
                        icon = Icons.Default.Star,
                        iconBg = ArrevaDarkTokens.IconBgGold,
                        iconTint = Color(0xFFFACC15),
                        label = "Favoris",
                        badgeText = if (isFavoriteAlertsEnabled) "★" else null,
                        testTag = "rail_btn_favorites",
                        onClick = {
                            onDismiss()
                            onOpenFavoritesManager()
                        }
                    )

                    // Item 4: Historique des Trajets
                    IconRailButton(
                        icon = Icons.Default.History,
                        iconBg = Color(0xFF1E293B),
                        iconTint = ArrevaDarkTokens.AmberGlow,
                        label = "Trajets",
                        testTag = "rail_btn_history",
                        onClick = {
                            onDismiss()
                            onOpenTripHistory()
                        }
                    )

                    // Item 5: Diagnostic Signal GPS
                    IconRailButton(
                        icon = Icons.Default.Radar,
                        iconBg = ArrevaDarkTokens.IconBgTeal,
                        iconTint = ArrevaDarkTokens.EmeraldGps,
                        label = "GPS",
                        badgeText = "●",
                        testTag = "rail_btn_gps",
                        onClick = { showGpsDetails = !showGpsDetails }
                    )

                    // Item 6: Activité en arrière-plan (Batterie)
                    IconRailButton(
                        icon = Icons.Default.ElectricBolt,
                        iconBg = ArrevaDarkTokens.IconBgEmerald,
                        iconTint = if (isBackgroundAllowed) ArrevaDarkTokens.EmeraldGps else ArrevaDarkTokens.AmberLight,
                        label = "Batterie",
                        badgeText = if (isBackgroundAllowed) "OK" else "!",
                        testTag = "rail_btn_battery",
                        onClick = { requestBackgroundActivity(context) }
                    )

                    // Item 7: Facebook
                    IconRailButton(
                        painter = painterResource(id = R.drawable.ic_facebook),
                        iconBg = Color(0xFF1877F2).copy(alpha = 0.2f),
                        label = "Facebook",
                        testTag = "rail_btn_facebook",
                        onClick = {
                            try {
                                val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1924SGgaKs/"))
                                context.startActivity(fbIntent)
                            } catch (_: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Small divider
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(1.dp)
                            .background(ArrevaDarkTokens.NavyBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Close Button
                    IconRailButton(
                        icon = Icons.Default.Close,
                        iconBg = ArrevaDarkTokens.NavyCard,
                        iconTint = ArrevaDarkTokens.TextLightSlate,
                        label = "Fermer",
                        testTag = "drawer_close_button",
                        onClick = onDismiss
                    )
                }
            } else {
                // =====================================================================
                // MODE PLEIN ÉCRAN DÉTAILLÉ (EXPANDED DRAWER)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "arreva",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArrevaDarkTokens.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ArrevaDarkTokens.AmberPrimary.copy(alpha = 0.35f))
                                            .border(BorderStroke(1.dp, ArrevaDarkTokens.AmberLight), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "GPS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArrevaDarkTokens.AmberGlow
                                        )
                                    }
                                }
                                Text(
                                    text = "Alerte d'arrivée",
                                    fontSize = 12.sp,
                                    color = ArrevaDarkTokens.TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Button to reduce to icons only
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArrevaDarkTokens.NavyCard)
                                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                                    .clickable { isIconOnlyMode = true }
                                    .testTag("btn_toggle_drawer_compact"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewSidebar,
                                    contentDescription = "Réduire aux icônes",
                                    tint = ArrevaDarkTokens.AmberLight,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArrevaDarkTokens.NavyCard)
                                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), CircleShape)
                                    .clickable(onClick = onDismiss)
                                    .testTag("drawer_close_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = ArrevaDarkTokens.TextLightSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // GPS Status Row
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

                    Text(
                        text = "NAVIGATION & TRAJETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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

                    Spacer(modifier = Modifier.height(10.dp))

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
                            AlarmTone.RADAR -> "Radar"
                            AlarmTone.BELL -> "Cloche"
                        },
                        onClick = {
                            onDismiss()
                            onOpenToneSelection()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 3: Favoris
                    DrawerMenuItem(
                        icon = Icons.Default.Star,
                        iconBg = ArrevaDarkTokens.IconBgGold,
                        iconTint = Color(0xFFFACC15),
                        title = "Favoris",
                        subtitle = if (isFavoriteAlertsEnabled) "Gares & arrêts • Alerte active" else "Gares et arrêts enregistrés",
                        badgeText = if (isFavoriteAlertsEnabled) "Alerte ON ✓" else "OFF",
                        onClick = {
                            onDismiss()
                            onOpenFavoritesManager()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 4: Historique des Trajets
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 5: Diagnostic & Signal GPS
                    DrawerMenuItem(
                        icon = Icons.Default.Explore,
                        iconBg = ArrevaDarkTokens.IconBgTeal,
                        iconTint = ArrevaDarkTokens.EmeraldGps,
                        title = "Diagnostic & Signal GPS",
                        subtitle = "Précision en mètres, vitesse et coordonnées",
                        onClick = { showGpsDetails = !showGpsDetails }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 6: Configuration AdMob / Publicité
                    DrawerMenuItem(
                        icon = Icons.Default.Campaign,
                        iconBg = ArrevaDarkTokens.IconBgPurple,
                        iconTint = Color(0xFFC084FC),
                        title = "Configuration AdMob / Publicité",
                        subtitle = "Google Mobile Ads & AdMob SDK",
                        badgeText = "Actif"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 7: Activité en arrière-plan
                    DrawerMenuItem(
                        icon = Icons.Default.ElectricBolt,
                        iconBg = ArrevaDarkTokens.IconBgEmerald,
                        iconTint = ArrevaDarkTokens.EmeraldGps,
                        title = "Activité en arrière-plan",
                        subtitle = if (isBackgroundAllowed) "Autorisé (sans restriction batterie)" else "Appuyer pour autoriser en arrière-plan",
                        badgeText = if (isBackgroundAllowed) "Actif ✓" else "Configurer",
                        onClick = { requestBackgroundActivity(context) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PRÉFÉRENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Facebook Link with small official logo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1877F2).copy(alpha = 0.12f))
                                    .border(BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.35f)), RoundedCornerShape(10.dp))
                                    .clickable {
                                        try {
                                            val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1924SGgaKs/"))
                                            context.startActivity(fbIntent)
                                        } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("btn_facebook_link"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_facebook),
                                        contentDescription = "Facebook",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Rejoignez-nous sur Facebook",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArrevaDarkTokens.TextPrimary
                                        )
                                        Text(
                                            text = "facebook.com/share/1924SGgaKs",
                                            fontSize = 10.sp,
                                            color = Color(0xFF60A5FA)
                                        )
                                    }
                                }

                                Text(
                                    text = "Ouvrir ↗",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating GPS Details Card when in icon-only mode
        if (isIconOnlyMode && showGpsDetails && userLocation != null) {
            Box(
                modifier = Modifier
                    .padding(start = 86.dp, top = 260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArrevaDarkTokens.NavySurface)
                    .border(BorderStroke(1.5.dp, ArrevaDarkTokens.EmeraldGps), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = ArrevaDarkTokens.EmeraldGps,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Signal GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lat: ${String.format("%.5f", userLocation.latitude)}\nLng: ${String.format("%.5f", userLocation.longitude)}\nPrécision: ±${userLocation.accuracyMeters.toInt()}m\nVitesse: ${userLocation.speedKmh.toInt()} km/h",
                        fontSize = 11.sp,
                        color = ArrevaDarkTokens.TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Compact Icon Button designed specifically for the Icon-Only rail
 */
@Composable
private fun IconRailButton(
    icon: ImageVector? = null,
    painter: Painter? = null,
    iconBg: Color = ArrevaDarkTokens.NavyCard,
    iconTint: Color = ArrevaDarkTokens.AmberLight,
    label: String,
    badgeText: String? = null,
    testTag: String = "",
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(21.dp)
                    )
                } else if (painter != null) {
                    Icon(
                        painter = painter,
                        contentDescription = label,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ArrevaDarkTokens.AmberPrimary)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArrevaDarkTokens.TextLightSlate,
            maxLines = 1
        )
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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Compact Rounded Square Icon Container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = ArrevaDarkTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ArrevaDarkTokens.TextSecondary
            )
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArrevaDarkTokens.TextLightSlate
                )
            }
        }
    }
}
