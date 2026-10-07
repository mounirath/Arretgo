package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AlarmTone
import com.example.model.AppLanguage
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Dialog to configure and preview alarm ringtones (Sonneries & Vibrations)
 */
@Composable
fun AlarmToneSelectionDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    selectedTone: AlarmTone,
    onSelectTone: (AlarmTone) -> Unit,
    isTestingTone: Boolean,
    onTestToneToggle: (AlarmTone) -> Unit,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    var previewTone by remember(selectedTone) { mutableStateOf(selectedTone) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(ArrevaDarkTokens.NavySurface)
                .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("alarm_tone_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ArrevaDarkTokens.AmberPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ArrevaDarkTokens.AmberLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "نغمات التنبيه"
                                    AppLanguage.EN -> "Alarm Ringtones"
                                    AppLanguage.FR -> "Sonneries d'alarme"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.TextPrimary
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "اختر النغمة واختبر الصوت"
                                    AppLanguage.EN -> "Choose tone & test audio"
                                    AppLanguage.FR -> "Sélectionnez et testez la sonnerie"
                                },
                                fontSize = 11.sp,
                                color = ArrevaDarkTokens.TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArrevaDarkTokens.TextLightSlate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tone Options List
                AlarmTone.values().forEach { tone ->
                    val isCurrent = tone == selectedTone
                    val isThisTesting = isTestingTone && previewTone == tone

                    val label = when (currentLanguage) {
                        AppLanguage.AR -> tone.labelAr
                        AppLanguage.EN -> tone.labelEn
                        AppLanguage.FR -> tone.labelFr
                    }

                    val desc = when (tone) {
                        AlarmTone.SIREN -> "Double tonalité d'urgence haute puissance"
                        AlarmTone.URGENT -> "Bips rapides et stridents cadencés"
                        AlarmTone.SOFT -> "Carillon mélodique apaisant à 3 notes"
                        AlarmTone.RADAR -> "Pings sous-marins résonnants type sonar"
                        AlarmTone.BELL -> "Cloche de quai ferroviaire traditionnelle"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) ArrevaDarkTokens.AmberPrimary.copy(alpha = 0.15f) else ArrevaDarkTokens.NavyCard)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isCurrent) ArrevaDarkTokens.AmberPrimary else ArrevaDarkTokens.NavyBorder
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                previewTone = tone
                                onSelectTone(tone)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) ArrevaDarkTokens.AmberLight else Color(0xFF1E293B)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) ArrevaDarkTokens.AmberLight else ArrevaDarkTokens.TextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = ArrevaDarkTokens.TextSecondary
                                    )
                                }
                            }

                            // Play / Preview Test Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isThisTesting) ArrevaDarkTokens.AmberPrimary else Color(0xFF1E293B)
                                    )
                                    .clickable {
                                        previewTone = tone
                                        onSelectTone(tone)
                                        onTestToneToggle(tone)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThisTesting) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Test tone",
                                    tint = if (isThisTesting) Color.Black else ArrevaDarkTokens.TextLightSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vibration Toggle
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
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = ArrevaDarkTokens.EmeraldGps,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "الاهتزاز عند الوصول"
                                    AppLanguage.EN -> "Vibrate on Arrival"
                                    AppLanguage.FR -> "Vibrations à l'approche"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArrevaDarkTokens.TextPrimary
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.AR -> "تأكيد حسي قوي"
                                    AppLanguage.EN -> "Haptic feedback pattern"
                                    AppLanguage.FR -> "Motif de vibration haptique cadencé"
                                },
                                fontSize = 11.sp,
                                color = ArrevaDarkTokens.TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = onVibrationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ArrevaDarkTokens.EmeraldGps,
                            uncheckedThumbColor = ArrevaDarkTokens.TextLightSlate,
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Validation Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArrevaDarkTokens.AmberPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.AR -> "تأكيد وحفظ"
                            AppLanguage.EN -> "Confirm & Save"
                            AppLanguage.FR -> "Confirmer la sonnerie"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
