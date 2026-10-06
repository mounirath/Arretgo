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
    alertRadiusMeters: Int,
    mapStyle: MapStyle,
    favorites: List<FavoritePlace> = emptyList(),
    onSelectFavorite: (FavoritePlace) -> Unit = {},
    onMapClick: (Double, Double) -> Unit,
    centerUserTrigger: Long = 0L,
    centerDestTrigger: Long = 0L,
    zoomInTrigger: Long = 0L,
    zoomOutTrigger: Long = 0L,
    modifier: Modifier = Modifier
) {
    var selectedFavorite by remember { mutableStateOf<FavoritePlace?>(null) }

    val initialPos = LatLng(userLocation.latitude, userLocation.longitude)
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
            }
        ) {
            // 1. User Location Marker
            Marker(
                state = MarkerState(position = LatLng(userLocation.latitude, userLocation.longitude)),
                title = "Votre position",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            // 2. Interactive Saved Favorite Stop Pins
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

            // 3. Destination Marker & Geofence Circle
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

        // 4. Interactive Callout View for Tapped Favorite Stop
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
