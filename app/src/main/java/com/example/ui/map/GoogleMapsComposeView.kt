package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoritePlace
import com.example.model.LocationPoint
import com.example.model.MapStyle
import com.example.model.UserLocation
import com.example.service.LocationTracker
import com.example.ui.components.formatDistance
import com.example.ui.theme.FrutigerAquaDeep
import com.example.ui.theme.FrutigerDeepNavy
import com.example.ui.theme.FrutigerGrassGreen
import com.example.ui.theme.FrutigerMeadowDark
import com.example.ui.theme.FrutigerSkyBlue
import com.example.ui.theme.FrutigerSlate
import com.example.ui.theme.GlassTokens
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GoogleMapsComposeView(
    userLocation: UserLocation,
    destination: LocationPoint?,
    pinnedStop: LocationPoint? = null,
    alertRadiusMeters: Int,
    mapStyle: MapStyle,
    favorites: List<FavoritePlace> = emptyList(),
    onSelectFavorite: (FavoritePlace) -> Unit = {},
    onMapClick: (Double, Double) -> Unit,
    onMapLongClick: (Double, Double) -> Unit = onMapClick,
    onSavePinnedStop: () -> Unit = {},
    onSetPinnedAsDestination: () -> Unit = {},
    onClearPinnedStop: () -> Unit = {},
    centerUserTrigger: Long = 0L,
    centerDestTrigger: Long = 0L,
    zoomInTrigger: Long = 0L,
    zoomOutTrigger: Long = 0L,
    modifier: Modifier = Modifier
) {
    var selectedFavorite by remember { mutableStateOf<FavoritePlace?>(null) }

    val initialPos = LatLng(
        destination?.latitude ?: pinnedStop?.latitude ?: userLocation.latitude,
        destination?.longitude ?: pinnedStop?.longitude ?: userLocation.longitude
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 14.5f)
    }

    val mapType = when (mapStyle) {
        MapStyle.OPENSTREETMAP -> MapType.NORMAL
        MapStyle.GOOGLE_MAPS -> MapType.NORMAL
        MapStyle.SATELLITE -> MapType.HYBRID
        MapStyle.TERRAIN -> MapType.TERRAIN
        MapStyle.DARK -> MapType.NORMAL
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            compassEnabled = true
        )
    }

    val properties = remember(mapType) {
        MapProperties(
            mapType = mapType,
            isMyLocationEnabled = false
        )
    }

    // Camera actions
    LaunchedEffect(centerUserTrigger) {
        if (centerUserTrigger > 0L) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.latitude, userLocation.longitude),
                    15.5f
                )
            )
        }
    }

    LaunchedEffect(destination?.latitude, destination?.longitude) {
        val dest = destination
        if (dest != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(dest.latitude, dest.longitude),
                    15.0f
                )
            )
        }
    }

    LaunchedEffect(pinnedStop?.latitude, pinnedStop?.longitude) {
        val pin = pinnedStop
        if (pin != null && destination == null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(pin.latitude, pin.longitude),
                    15.0f
                )
            )
        }
    }

    LaunchedEffect(centerDestTrigger) {
        if (centerDestTrigger > 0L && destination != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(destination.latitude, destination.longitude),
                    15.5f
                )
            )
        }
    }

    LaunchedEffect(zoomInTrigger) {
        if (zoomInTrigger > 0L) {
            cameraPositionState.animate(CameraUpdateFactory.zoomIn())
        }
    }

    LaunchedEffect(zoomOutTrigger) {
        if (zoomOutTrigger > 0L) {
            cameraPositionState.animate(CameraUpdateFactory.zoomOut())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapClick = { latLng ->
                selectedFavorite = null
                onMapClick(latLng.latitude, latLng.longitude)
            },
            onMapLongClick = { latLng ->
                selectedFavorite = null
                onMapLongClick(latLng.latitude, latLng.longitude)
            }
        ) {
            // 1. User Current GPS Location Marker & Presence Pulse Circle
            Marker(
                state = MarkerState(position = LatLng(userLocation.latitude, userLocation.longitude)),
                title = "Votre position",
                snippet = "GPS actif (±%.0f m)".format(userLocation.accuracyMeters),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            Circle(
                center = LatLng(userLocation.latitude, userLocation.longitude),
                radius = userLocation.accuracyMeters.toDouble().coerceIn(15.0, 50.0),
                fillColor = Color(0x2200E5FF),
                strokeColor = Color(0x8000E5FF),
                strokeWidth = 2f
            )

            // 2. Interactive Saved Favorite Transit Stop Pins from Room
            favorites.forEach { fav ->
                val favLatLng = LatLng(fav.latitude, fav.longitude)
                Marker(
                    state = MarkerState(position = favLatLng),
                    title = "★ ${fav.name}",
                    snippet = "${fav.tag} • ${fav.defaultRadiusMeters}m - Touchez pour sélectionner",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                    onClick = {
                        selectedFavorite = fav
                        false
                    },
                    onInfoWindowClick = {
                        onSelectFavorite(fav)
                        selectedFavorite = null
                    }
                )
            }

            // 2b. Visual Custom Alert Radius Geofence Circle for selected favorite stop
            selectedFavorite?.let { fav ->
                Circle(
                    center = LatLng(fav.latitude, fav.longitude),
                    radius = fav.defaultRadiusMeters.toDouble(),
                    fillColor = Color(0x2EF59E0B),
                    strokeColor = Color(0xFFF59E0B),
                    strokeWidth = 3f
                )
            }

            // 3. Pinned Transit Stop Marker & Visual Geofence Radius Preview
            pinnedStop?.let { pin ->
                val pinLatLng = LatLng(pin.latitude, pin.longitude)
                Marker(
                    state = MarkerState(position = pinLatLng),
                    title = "📌 ${pin.name}",
                    snippet = "Arrêt de transport épinglé - Touchez pour enregistrer",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )

                Circle(
                    center = pinLatLng,
                    radius = alertRadiusMeters.toDouble(),
                    fillColor = Color(0x22A855F7),
                    strokeColor = Color(0xFFA855F7),
                    strokeWidth = 3f
                )
            }

            // 4. Destination Marker & Geofence Circle
            if (destination != null) {
                val destLatLng = LatLng(destination.latitude, destination.longitude)

                Marker(
                    state = MarkerState(position = destLatLng),
                    title = destination.name,
                    snippet = "Rayon de réveil: $alertRadiusMeters m",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                Circle(
                    center = destLatLng,
                    radius = alertRadiusMeters.toDouble(),
                    fillColor = Color(0x3000E5FF),
                    strokeColor = Color(0xFF00E5FF),
                    strokeWidth = 4f
                )

                // Polyline connecting User and Destination
                Polyline(
                    points = listOf(
                        LatLng(userLocation.latitude, userLocation.longitude),
                        destLatLng
                    ),
                    color = Color(0xFF1A73E8),
                    width = 8f
                )
            }
        }

        // 5. Interactive Callout View for Pinned Transit Stop
        AnimatedVisibility(
            visible = pinnedStop != null && selectedFavorite == null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            val isDarkTerrain = mapStyle == MapStyle.SATELLITE || mapStyle == MapStyle.DARK
            pinnedStop?.let { pin ->
                PinnedTransitStopCard(
                    pinnedStop = pin,
                    userLocation = userLocation,
                    isDarkTerrain = isDarkTerrain,
                    onSaveToFavorites = onSavePinnedStop,
                    onSetAsDestination = onSetPinnedAsDestination,
                    onDismiss = onClearPinnedStop
                )
            }
        }

        // 6. Interactive Callout View for Tapped Favorite Stop
        AnimatedVisibility(
            visible = selectedFavorite != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            val isDarkTerrain = mapStyle == MapStyle.SATELLITE || mapStyle == MapStyle.DARK
            selectedFavorite?.let { fav ->
                FavoriteStopCalloutCard(
                    favorite = fav,
                    userLocation = userLocation,
                    isDarkTerrain = isDarkTerrain,
                    onSetAsDestination = {
                        onSelectFavorite(fav)
                        selectedFavorite = null
                    },
                    onDismiss = { selectedFavorite = null }
                )
            }
        }
    }
}

/**
 * Liquid Glass Interactive Callout Card for Favorite Stops
 */
@Composable
fun FavoriteStopCalloutCard(
    favorite: FavoritePlace,
    userLocation: UserLocation,
    isDarkTerrain: Boolean = false,
    onSetAsDestination: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distance = remember(userLocation.latitude, userLocation.longitude, favorite.latitude, favorite.longitude) {
        LocationTracker.calculateDistanceMeters(
            userLocation.latitude,
            userLocation.longitude,
            favorite.latitude,
            favorite.longitude
        )
    }

    val textColor = if (isDarkTerrain) com.example.ui.theme.LiquidGlassTokens.DarkTextPrimary else com.example.ui.theme.LiquidGlassTokens.LightTextPrimary
    val textSecondaryColor = if (isDarkTerrain) com.example.ui.theme.LiquidGlassTokens.DarkTextSecondary else com.example.ui.theme.LiquidGlassTokens.LightTextSecondary

    com.example.ui.theme.LiquidGlassCapsule(
        modifier = modifier
            .fillMaxWidth()
            .testTag("favorite_callout_view"),
        isDarkTerrain = isDarkTerrain,
        shape = RoundedCornerShape(26.dp),
        elevation = 20.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Star Tag Badge & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gold Tag Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(BorderStroke(1.dp, Color(0xFFFCD34D)), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Arrêt Favori • ${favorite.tag}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer l'aperçu",
                        tint = FrutigerSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stop Name
            Text(
                text = favorite.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FrutigerDeepNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (favorite.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = favorite.address,
                    fontSize = 12.sp,
                    color = FrutigerSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Pills: Distance & Default Radius
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Distance Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(1.dp, Color(0xFFBAE6FD)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = FrutigerAquaDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDistance(distance),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrutigerMeadowDark
                        )
                    }
                }

                // Alert Radius Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(1.dp, Color(0xFFBBF7D0)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🔔 Réveil à ${favorite.defaultRadiusMeters} m",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Set as Destination (Aero Gel Button)
            Button(
                onClick = onSetAsDestination,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0284C7).copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                FrutigerSkyBlue,
                                FrutigerAquaDeep,
                                FrutigerGrassGreen
                            )
                        )
                    )
                    .border(BorderStroke(1.5.dp, Color.White), RoundedCornerShape(16.dp))
                    .testTag("btn_callout_set_destination")
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(GlassTokens.GlossCapBrush)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Définir comme destination",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Interactive Callout Card for Pinned Transit Stops
 */
@Composable
fun PinnedTransitStopCard(
    pinnedStop: LocationPoint,
    userLocation: UserLocation,
    isDarkTerrain: Boolean = false,
    onSaveToFavorites: () -> Unit,
    onSetAsDestination: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distance = remember(userLocation.latitude, userLocation.longitude, pinnedStop.latitude, pinnedStop.longitude) {
        LocationTracker.calculateDistanceMeters(
            userLocation.latitude,
            userLocation.longitude,
            pinnedStop.latitude,
            pinnedStop.longitude
        )
    }

    com.example.ui.theme.LiquidGlassCapsule(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pinned_transit_stop_card"),
        isDarkTerrain = isDarkTerrain,
        shape = RoundedCornerShape(24.dp),
        elevation = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Pin Badge & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDE9FE))
                        .border(BorderStroke(1.dp, Color(0xFFC4B5FD)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🚏 Arrêt de transport épinglé",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B21B6)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_dismiss_pinned_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer l'épingle",
                        tint = FrutigerSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stop Name
            Text(
                text = pinnedStop.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FrutigerDeepNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (pinnedStop.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pinnedStop.address,
                    fontSize = 12.sp,
                    color = FrutigerSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Distance Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(1.dp, Color(0xFFBAE6FD)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = FrutigerAquaDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "À ${formatDistance(distance)} de votre position",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrutigerMeadowDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: [⭐ Enregistrer l'arrêt] & [🎯 Définir destination]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button: Save to Favorites
                Button(
                    onClick = onSaveToFavorites,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_save_pinned_stop")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enregistrer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Button: Set as Destination
                Button(
                    onClick = onSetAsDestination,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_set_pinned_destination")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Destination",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

