package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.TripState
import com.example.model.UserLocation
import com.example.ui.theme.ArrevaDarkTokens

@Composable
fun TripHud(
    tripState: TripState,
    userLocation: UserLocation,
    currentLanguage: AppLanguage,
    onStopTrip: () -> Unit,
    isDarkTerrain: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = tripState.isActive,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(20.dp))
                .background(ArrevaDarkTokens.NavySheet.copy(alpha = 0.96f))
                .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header: Destination name & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Pulsing Live Beacon
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .shadow(6.dp, CircleShape, spotColor = ArrevaDarkTokens.EmeraldGps)
                                .clip(CircleShape)
                                .background(if (tripState.isWithinAlertZone) Color(0xFFEF4444) else ArrevaDarkTokens.EmeraldGps)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (tripState.isWithinAlertZone) {
                                    when (currentLanguage) {
                                        AppLanguage.AR -> "🚨 في منطقة التنبيه!"
                                        AppLanguage.EN -> "🚨 Inside alert zone!"
                                        AppLanguage.FR -> "🚨 Dans la zone d'alerte !"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        AppLanguage.AR -> "رحلة جارية نحو:"
                                        AppLanguage.EN -> "En route to:"
                                        AppLanguage.FR -> "Trajet en cours vers :"
                                    }
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tripState.isWithinAlertZone) Color(0xFFEF4444) else ArrevaDarkTokens.EmeraldGps
                            )
                            Text(
                                text = tripState.destination?.name ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ArrevaDarkTokens.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Stop trip Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .testTag("hud_stop_trip_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onStopTrip) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Arrêter le trajet",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metrics Row: Remaining Distance, Alert Threshold, Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distance Remaining
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "المسافة المتبقية"
                                AppLanguage.EN -> "Distance Remaining"
                                AppLanguage.FR -> "Distance restante"
                            },
                            fontSize = 11.sp,
                            color = ArrevaDarkTokens.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (tripState.currentDistanceMeters < Float.MAX_VALUE) {
                                formatDistance(tripState.currentDistanceMeters)
                            } else "--",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ArrevaDarkTokens.AmberLight
                        )
                    }

                    // Speed Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArrevaDarkTokens.NavyCard)
                            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ArrevaDarkTokens.EmeraldGps,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userLocation.speedKmh.toInt()} km/h",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.TextPrimary
                            )
                        }
                    }

                    // Alert Radius Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArrevaDarkTokens.NavyCard)
                            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = ArrevaDarkTokens.AmberLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${tripState.alertRadiusMeters} m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar toward Alert Zone
                val progress = if (tripState.initialDistanceMeters > 0f) {
                    val covered = tripState.initialDistanceMeters - tripState.currentDistanceMeters
                    (covered / tripState.initialDistanceMeters).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = if (tripState.isWithinAlertZone) Color(0xFFEF4444) else ArrevaDarkTokens.AmberPrimary,
                    trackColor = ArrevaDarkTokens.NavyCard
                )
            }
        }
    }
}
