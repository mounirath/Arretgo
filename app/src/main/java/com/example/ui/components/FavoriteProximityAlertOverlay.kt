package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.FavoriteAlertEvent
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Favorite Proximity Alert Overlay
 *
 * Triggered automatically when the user gets near any saved favorite place
 * (within its designated alert radius, e.g. 500m).
 * Plays sound, vibrates, and offers quick actions (stop alarm, mute, set as destination).
 */
@Composable
fun FavoriteProximityAlertOverlay(
    alertEvent: FavoriteAlertEvent?,
    currentLanguage: AppLanguage,
    onDismissAlert: () -> Unit,
    onMuteAlert: () -> Unit,
    onSetAsDestination: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = alertEvent != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        if (alertEvent == null) return@AnimatedVisibility

        val infiniteTransition = rememberInfiniteTransition(label = "pulse_star")
        val starScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(650),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp), spotColor = ArrevaDarkTokens.AmberLight)
                .clip(RoundedCornerShape(22.dp))
                .background(ArrevaDarkTokens.NavySheet.copy(alpha = 0.98f))
                .border(
                    BorderStroke(2.dp, ArrevaDarkTokens.AmberLight),
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
                .testTag("favorite_proximity_alert_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsing Golden Star Icon Badge
                Box(
                    modifier = Modifier
                        .scale(starScale)
                        .size(56.dp)
                        .shadow(12.dp, CircleShape, spotColor = ArrevaDarkTokens.AmberGlow)
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
                        imageVector = Icons.Default.Star,
                        contentDescription = "Lieu Favori",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "⭐ تنبيه: اقتربت من مكانك المفضل!"
                        AppLanguage.EN -> "⭐ ALERT: Approaching Favorite Place!"
                        AppLanguage.FR -> "⭐ ALERTE : Arrêt favori à proximité !"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = ArrevaDarkTokens.AmberLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Favorite Name
                Text(
                    text = alertEvent.favorite.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ArrevaDarkTokens.TextPrimary,
                    textAlign = TextAlign.Center
                )

                if (alertEvent.favorite.address.isNotBlank()) {
                    Text(
                        text = alertEvent.favorite.address,
                        fontSize = 12.sp,
                        color = ArrevaDarkTokens.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Proximity Distance Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArrevaDarkTokens.NavyCard)
                        .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.AR -> "المسافة الحالية: ${formatDistance(alertEvent.distanceMeters)} (نطاق: ${alertEvent.favorite.defaultRadiusMeters}م)"
                            AppLanguage.EN -> "Current distance: ${formatDistance(alertEvent.distanceMeters)} (zone: ${alertEvent.favorite.defaultRadiusMeters}m)"
                            AppLanguage.FR -> "Distance actuelle : ${formatDistance(alertEvent.distanceMeters)} (zone : ${alertEvent.favorite.defaultRadiusMeters} m)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.EmeraldGps
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Dismiss / Acknowledge Alarm
                    Button(
                        onClick = onDismissAlert,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArrevaDarkTokens.AmberPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_dismiss_favorite_alert")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ArrevaDarkTokens.TextOnAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "إيقاف التنبيه"
                                    AppLanguage.EN -> "Stop Alarm"
                                    AppLanguage.FR -> "Arrêter l'alarme"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = ArrevaDarkTokens.TextOnAmber
                            )
                        }
                    }

                    // 2. Set as Destination
                    OutlinedButton(
                        onClick = onSetAsDestination,
                        border = BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "توجيه نحو المكان"
                                    AppLanguage.EN -> "Navigate here"
                                    AppLanguage.FR -> "Suivre ce lieu"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.TextLightSlate
                            )
                        }
                    }

                    // 3. Mute Toggle if ringing
                    if (alertEvent.isRinging && !alertEvent.isMuted) {
                        OutlinedButton(
                            onClick = onMuteAlert,
                            border = BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                                contentDescription = "Silence",
                                tint = ArrevaDarkTokens.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
