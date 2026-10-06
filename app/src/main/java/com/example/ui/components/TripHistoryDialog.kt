package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RecentTrip
import com.example.model.AppLanguage
import com.example.model.LocationPoint
import com.example.ui.theme.ArrevaDarkTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trip History Screen / Dialog
 *
 * Allows viewing all recent trips completed by the user with:
 * - Date and time
 * - Destination name & address
 * - Total distance & alert radius
 * - Quick re-launch ("Relancer ce trajet")
 * - Quick add to favorites ("⭐ Enregistrer")
 * - Deletion / Clear history
 */
@Composable
fun TripHistoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    recentTrips: List<RecentTrip>,
    onRelaunchTrip: (RecentTrip) -> Unit,
    onSaveToFavorites: (RecentTrip) -> Unit,
    onDeleteTrip: (RecentTrip) -> Unit,
    onClearAllHistory: () -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(ArrevaDarkTokens.NavySurface)
                    .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .testTag("dialog_trip_history")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArrevaDarkTokens.IconBgAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = ArrevaDarkTokens.AmberGlow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> "سجل الرحلات السابقة"
                                        AppLanguage.EN -> "Recent Trip History"
                                        AppLanguage.FR -> "Historique des Trajets"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArrevaDarkTokens.TextPrimary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> "${recentTrips.size} رحلة مسجلة"
                                        AppLanguage.EN -> "${recentTrips.size} saved trips"
                                        AppLanguage.FR -> "${recentTrips.size} trajets enregistrés"
                                    },
                                    fontSize = 12.sp,
                                    color = ArrevaDarkTokens.TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (recentTrips.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearAllHistory,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Effacer l'historique",
                                        tint = ArrevaDarkTokens.TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = ArrevaDarkTokens.TextLightSlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (recentTrips.isEmpty()) {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ArrevaDarkTokens.NavyCard)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = ArrevaDarkTokens.TextTertiary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> "لا توجد رحلات سابقة مسجلة حتى الآن."
                                        AppLanguage.EN -> "No recent trips recorded yet."
                                        AppLanguage.FR -> "Aucun trajet récent enregistré."
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArrevaDarkTokens.TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> "ستظهر رحلاتك هنا تلقائياً عند بدء ومتابعة أي مسار."
                                        AppLanguage.EN -> "Your trips will automatically appear here once tracked."
                                        AppLanguage.FR -> "Vos trajets s'enregistreront ici dès que vous lancez un suivi."
                                    },
                                    fontSize = 12.sp,
                                    color = ArrevaDarkTokens.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Trips List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentTrips, key = { it.id }) { trip ->
                                TripHistoryItemCard(
                                    trip = trip,
                                    onRelaunch = {
                                        onRelaunchTrip(trip)
                                        onDismiss()
                                    },
                                    onSaveFavorite = { onSaveToFavorites(trip) },
                                    onDelete = { onDeleteTrip(trip) },
                                    currentLanguage = currentLanguage
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AdMob Test Banner
                    AdMobBanner(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun TripHistoryItemCard(
    trip: RecentTrip,
    onRelaunch: () -> Unit,
    onSaveFavorite: () -> Unit,
    onDelete: () -> Unit,
    currentLanguage: AppLanguage
) {
    val dateString = remember(trip.completedAt) {
        val sdf = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
        sdf.format(Date(trip.completedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ArrevaDarkTokens.NavyCard)
            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Icon, Destination Title & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ArrevaDarkTokens.IconBgBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Train,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip.destinationName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (trip.destinationAddress.isNotBlank()) {
                            Text(
                                text = trip.destinationAddress,
                                fontSize = 11.sp,
                                color = ArrevaDarkTokens.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = ArrevaDarkTokens.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Date, Distance & Arrival Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    fontSize = 11.sp,
                    color = ArrevaDarkTokens.TextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trip.initialDistanceMeters > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ArrevaDarkTokens.NavySurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${formatDistance(trip.initialDistanceMeters)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.AmberLight
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArrevaDarkTokens.EmeraldBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✓ Arrivé (${trip.alertRadiusMeters}m)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.EmeraldGps
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Action Buttons (Relancer ce trajet & Sauvegarder en favori)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Relancer le trajet
                Button(
                    onClick = onRelaunch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArrevaDarkTokens.AmberPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ArrevaDarkTokens.TextOnAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "إعادة الرحلة"
                                AppLanguage.EN -> "Restart trip"
                                AppLanguage.FR -> "Relancer"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.TextOnAmber
                        )
                    }
                }

                // Sauvegarder en favori
                OutlinedButton(
                    onClick = onSaveFavorite,
                    border = BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ArrevaDarkTokens.AmberGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "حفظ"
                                AppLanguage.EN -> "Favorite"
                                AppLanguage.FR -> "Favori"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArrevaDarkTokens.TextLightSlate
                        )
                    }
                }
            }
        }
    }
}
