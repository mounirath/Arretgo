package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.TripState

@Composable
fun AlarmOverlay(
    tripState: TripState,
    currentLanguage: AppLanguage,
    onDismissAlarm: () -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = tripState.isAlarmRinging,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "alarm")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        val bellShake by infiniteTransition.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(150),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE11D48).copy(alpha = 0.96f),
                            Color(0xFF881337).copy(alpha = 0.98f),
                            Color(0xFF0F172A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Flashing and swinging Alarm Bell
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarme Réveil",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier
                                .size(50.dp)
                                .rotate(bellShake)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Urgent Headline
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "🚨 تنبيه: اقتربت من محطتك!"
                        AppLanguage.EN -> "🚨 ARRIVING AT YOUR STOP!"
                        AppLanguage.FR -> "🚨 ARRIVÉE IMMINENTE !"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "أنت الآن داخل نطاق التنبيه (${formatDistance(tripState.alertRadiusMeters)})"
                        AppLanguage.EN -> "You have entered your alert radius (${formatDistance(tripState.alertRadiusMeters)})"
                        AppLanguage.FR -> "Vous êtes entré dans le rayon d'alerte (${formatDistance(tripState.alertRadiusMeters)})"
                    },
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Destination Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tripState.destination?.name ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        if (!tripState.destination?.address.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tripState.destination?.address ?: "",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Distance restante : ${formatDistance(tripState.currentDistanceMeters)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE047)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Button(
                    onClick = onDismissAlarm,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("dismiss_alarm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFFE11D48)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.AR -> "إيقاف المنبه والرحلة"
                            AppLanguage.EN -> "Stop Alarm & Finish Trip"
                            AppLanguage.FR -> "Arrêter le réveil (Arrivé)"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11D48)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mute Sound Toggle Button
                OutlinedButton(
                    onClick = onMuteToggle,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("mute_alarm_button")
                ) {
                    Icon(
                        imageVector = if (tripState.isAlarmMuted) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tripState.isAlarmMuted) {
                            when (currentLanguage) {
                                AppLanguage.AR -> "إعادة تشغيل الصوت"
                                AppLanguage.EN -> "Unmute Alarm"
                                AppLanguage.FR -> "Réactiver le son"
                            }
                        } else {
                            when (currentLanguage) {
                                AppLanguage.AR -> "كتم الصوت فقط"
                                AppLanguage.EN -> "Mute Sound Only"
                                AppLanguage.FR -> "Couper le son uniquement"
                            }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
