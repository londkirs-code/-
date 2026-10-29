package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CountdownUiState
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.keypadFocusable

@Composable
fun VatikinAlarmCard(
    state: CountdownUiState,
    onToggleAlarm: (Boolean) -> Unit,
    onChangeMinutes: (Int) -> Unit,
    onSetMinutes: (Int) -> Unit,
    onTestAlarm: () -> Unit,
    onSyncSystemClock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleAlarm(true)
        } else {
            Toast.makeText(context, "שים לב: ללא הרשאת התרעות לא יופיעו התרעות שעון במסך הנעילה", Toast.LENGTH_LONG).show()
            onToggleAlarm(true)
        }
    }

    Surface(
        color = colors.surfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (state.alarmEnabled) 1.5.dp else 1.dp,
            color = if (state.alarmEnabled) colors.gold else colors.border
        ),
        modifier = modifier
            .fillMaxWidth()
            .keypadFocusable(shape = RoundedCornerShape(16.dp))
            .testTag("vatikin_alarm_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon, Title, and Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.alarmEnabled) colors.gold.copy(alpha = 0.20f) else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.alarmEnabled) Icons.Default.AlarmOn else Icons.Default.Alarm,
                            contentDescription = "שעון מעורר",
                            tint = if (state.alarmEnabled) colors.gold else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "שעון מעורר ותיקין דינמי",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (state.alarmEnabled) "מתעדכן יומי לפי הנץ" else "כבוי",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (state.alarmEnabled) colors.gold else colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = state.alarmEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onToggleAlarm(enable)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.gold,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surfaceVariant
                    ),
                    modifier = Modifier.testTag("alarm_master_switch")
                )
            }

            // Status Banner with Target Time
            Surface(
                color = if (state.alarmEnabled) colors.gold.copy(alpha = 0.12f) else colors.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.alarmEnabled) colors.gold.copy(alpha = 0.4f) else colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.alarmEnabled) "שעת הצלצול הקרובה:" else "השעון יצלצל ב:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${state.alarmNextTriggerFormatted} (${state.alarmMinutesBeforeNetz} דק' לפני הנץ)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = if (state.alarmEnabled) colors.gold else colors.textPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (state.alarmEnabled) colors.gold.copy(alpha = 0.20f) else colors.surfaceVariant
                    ) {
                        Text(
                            text = if (state.alarmEnabled) "פעיל מחר" else "כבוי",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (state.alarmEnabled) colors.gold else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Stepper: Minutes before Netz
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "זמן השכמה לפני נץ החמה:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = colors.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { onChangeMinutes(-5) },
                            enabled = state.alarmMinutesBeforeNetz > 5,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(40.dp)
                                .keypadFocusable(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = "פחות 5 דקות", modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .width(96.dp)
                                .height(40.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${state.alarmMinutesBeforeNetz} דק'",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = colors.gold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onChangeMinutes(5) },
                            enabled = state.alarmMinutesBeforeNetz < 180,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(40.dp)
                                .keypadFocusable(shape = RoundedCornerShape(10.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "עוד 5 דקות", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(
                        text = "מתעדכן יומי אוטומטי",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = colors.textSecondary
                    )
                }

                // Quick Preset Chips (30, 45, 60, 75, 90 minutes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(30, 45, 60, 75, 90)
                    presets.forEach { mins ->
                        val isSelected = state.alarmMinutesBeforeNetz == mins
                        Button(
                            onClick = { onSetMinutes(mins) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.gold else colors.surfaceVariant,
                                contentColor = if (isSelected) (if (colors.isDark) Color(0xFF0F172A) else Color.White) else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .keypadFocusable(shape = RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "$mins דק'",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Quick Actions: Test Alarm & Sync with Phone System Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestAlarm,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .keypadFocusable(shape = RoundedCornerShape(10.dp))
                        .testTag("test_alarm_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "בדיקת צלצול",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = colors.gold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSyncSystemClock,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .keypadFocusable(shape = RoundedCornerShape(10.dp))
                        .testTag("sync_system_clock_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "סנכרן לשעון המכשיר",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
