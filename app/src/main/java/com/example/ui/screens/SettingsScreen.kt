package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode
import com.example.model.LocationData
import com.example.model.PredefinedLocations
import com.example.ui.components.VatikinAlarmCard
import com.example.ui.CountdownUiState
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    state: CountdownUiState,
    onSelectLocation: (LocationData) -> Unit,
    onDetectLocation: () -> Unit,
    onSetPlainNetz: (Boolean) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleTenSecBeep: () -> Unit,
    onSetUiScale: (Float) -> Unit = {},
    onSaveCustomLocation: (name: String, lat: Double, lon: Double, elevation: Double, offset: Int) -> Unit,
    onToggleAlarm: (Boolean) -> Unit = {},
    onChangeAlarmMinutes: (Int) -> Unit = {},
    onSetAlarmMinutes: (Int) -> Unit = {},
    onToggleAlarmSound: (Boolean) -> Unit = {},
    onToggleAlarmVibration: (Boolean) -> Unit = {},
    onSetAlarmSnoozeMinutes: (Int) -> Unit = {},
    onTestAlarm: () -> Unit = {},
    onSyncSystemClock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var showCityDialog by remember { mutableStateOf(false) }
    var showCustomLocationDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onDetectLocation()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: UI Scaling (הגדלה / הקטנה של אלמנטים, פונטים וכפתורים)
        item {
            Text(
                text = "גודל תצוגה, פונטים וכפתורים",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = colors.gold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "קנה מידה: ${(state.uiScale * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = colors.textPrimary
                            )
                        }

                        // Plus / Minus Steppers (accessible 44dp size)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onSetUiScale(state.uiScale - 0.15f) },
                                enabled = state.uiScale > 0.82f,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(44.dp)
                                    .keypadFocusable(shape = RoundedCornerShape(12.dp))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Remove, contentDescription = "הקטן", modifier = Modifier.size(20.dp))
                                }
                            }

                            OutlinedButton(
                                onClick = { onSetUiScale(state.uiScale + 0.15f) },
                                enabled = state.uiScale < 1.35f,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(44.dp)
                                    .keypadFocusable(shape = RoundedCornerShape(12.dp))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "הגדל", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons (height 42dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            0.85f to "קומפקטי",
                            1.00f to "רגיל",
                            1.15f to "גדול",
                            1.30f to "ענק"
                        )
                        presets.forEach { (scaleVal, label) ->
                            val isSelected = kotlin.math.abs(state.uiScale - scaleVal) < 0.05f
                            Button(
                                onClick = { onSetUiScale(scaleVal) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) colors.gold else colors.surfaceVariant,
                                    contentColor = if (isSelected) (if (colors.isDark) DawnDeepNight else androidx.compose.ui.graphics.Color.White) else colors.textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .keypadFocusable(shape = RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Theme Mode Section (מצב יום / מצב לילה)
        item {
            Text(
                text = "עיצוב: מצב יום / לילה",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AppThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetThemeMode(mode) }
                                .keypadFocusable(shape = RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.titleHebrew,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (state.themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.5.sp
                                ),
                                color = if (state.themeMode == mode) colors.gold else colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Location Section
        item {
            Text(
                text = "מיקום וקואורדינטות",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "מיקום פעיל:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "קואורדינטות: ${String.format("%.4f", state.selectedLocation.latitude)}°N, ${String.format("%.4f", state.selectedLocation.longitude)}°E | גובה: ${state.selectedLocation.elevationMeters.toInt()} מ'",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = colors.textGold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // שורת כפתורים 1: שתי פעולות בגובה 50dp מרווח ומיושר
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showCityDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.gold,
                                contentColor = if (colors.isDark) DawnDeepNight else androidx.compose.ui.graphics.Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .keypadFocusable(shape = RoundedCornerShape(12.dp))
                                .testTag("choose_city_btn")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "בחר עיר",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .keypadFocusable(shape = RoundedCornerShape(12.dp))
                                .testTag("gps_detect_btn"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.8f))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isLocating) "מאתר..." else "זיהוי GPS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp
                                    ),
                                    color = colors.gold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // שורת כפתורים 2: הגדרה ידנית ברוחב מלא ובגובה 50dp
                    OutlinedButton(
                        onClick = { showCustomLocationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .keypadFocusable(shape = RoundedCornerShape(12.dp))
                            .testTag("custom_location_btn"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocation,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "הגדרת מיקום מותאם אישית",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp
                                ),
                                color = colors.textPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    if (state.locationError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.locationError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.rose
                        )
                    }
                }
            }
        }

        // Calculation Mode Section
        item {
            Text(
                text = "סוג חישוב נץ החמה",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSetPlainNetz(false) }
                            .keypadFocusable(shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !state.isPlainNetz,
                            onClick = { onSetPlainNetz(false) },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "נץ נראה (טופוגרפי) - מומלץ לוותיקין",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp
                                ),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "הרגע בו גוף השמש עולה בפועל מעל קו ההרים המקומיים (לוח ארץ ישראל).",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = colors.textSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSetPlainNetz(true) }
                            .keypadFocusable(shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.isPlainNetz,
                            onClick = { onSetPlainNetz(true) },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "נץ מישורי (אסטרונומי)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp
                                ),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "זריחה אסטרונומית ישרה בגובה פני הים ללא מחסומי הרים.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Vatikin Alarm Clock Section
        item {
            Text(
                text = "שעון מעורר ותיקין דינמי",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            VatikinAlarmCard(
                state = state,
                onToggleAlarm = onToggleAlarm,
                onChangeMinutes = onChangeAlarmMinutes,
                onSetMinutes = onSetAlarmMinutes,
                onTestAlarm = onTestAlarm,
                onSyncSystemClock = onSyncSystemClock
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "צליל בשעון מעורר",
                        subtitle = "הפעלת צלצול התרעה חזק",
                        checked = state.alarmSoundEnabled,
                        onCheckedChange = { onToggleAlarmSound(!state.alarmSoundEnabled) }
                    )

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "רטט בשעון מעורר",
                        subtitle = "הפעלת רטט חוזר בעת צלצול",
                        checked = state.alarmVibrationEnabled,
                        onCheckedChange = { onToggleAlarmVibration(!state.alarmVibrationEnabled) }
                    )

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 8.dp))

                    // Snooze duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "משך נודניק (Snooze)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "זמן המתנה עד לצלצול הבא",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = colors.textSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 15).forEach { mins ->
                                val isSelected = state.alarmSnoozeMinutes == mins
                                Button(
                                    onClick = { onSetAlarmSnoozeMinutes(mins) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) colors.gold else colors.surfaceVariant,
                                        contentColor = if (isSelected) (if (colors.isDark) Color(0xFF0F172A) else Color.White) else colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .keypadFocusable(shape = RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = "$mins דק'",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sound & Alerts Section
        item {
            Text(
                text = "צלילים והתראות רטט",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = colors.gold
            )
        }

        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "הפעלת צלילי התראה",
                        subtitle = "צלצול צפצוף ברגע הנץ ובנקודות ציון",
                        checked = state.soundEnabled,
                        onCheckedChange = { onToggleSound() }
                    )

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "רטט (Haptics)",
                        subtitle = "רטט שקט בעשר השניות וברגע הנץ",
                        checked = state.vibrationEnabled,
                        onCheckedChange = { onToggleVibration() }
                    )

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "צפצוף בעשר השניות האחרונות",
                        subtitle = "ספירה קולית לתזמון סיום גאל ישראל",
                        checked = state.tenSecBeepEnabled,
                        onCheckedChange = { onToggleTenSecBeep() }
                    )
                }
            }
        }

        // Keypad Phone Guide (updated with reversed * and # actions and key 9 scale toggle)
        item {
            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = colors.gold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "תמיכה במכשירי מקשים (כשרים):",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• כפתור * למעבר הבא, כפתור # למעבר קודם\n" +
                                "• ספרות 1 עד 5 למעבר ישיר בין הלשוניות\n" +
                                "• ספרה 7 להחלפת מצב יום / לילה\n" +
                                "• ספרה 8 להחלפת נץ נראה / מישורי\n" +
                                "• ספרה 9 לשינוי גודל תצוגה ופונטים\n" +
                                "• ספרה 0 להשתקת צלילים\n" +
                                "• חצי D-Pad ומקש Enter לבחירה עם מסגרת סימון ברורה",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = colors.textSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "נץ החמה • תצוגה מותאמת אישית • גרסה 1.3",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = colors.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // City Selection Dialog (Compact & Keypad-Friendly)
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "בחר עיר או יישוב",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = colors.textPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    items(PredefinedLocations.CITIES) { city ->
                        val isSelected = state.selectedLocation.id == city.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectLocation(city)
                                    showCityDialog = false
                                }
                                .keypadFocusable(shape = RoundedCornerShape(10.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.nameHebrew,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 16.5.sp
                                    ),
                                    color = if (isSelected) colors.gold else colors.textPrimary
                                )
                                Text(
                                    text = "${city.nameEnglish} • ${city.elevationMeters.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = colors.textSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCityDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = if (colors.isDark) DawnDeepNight else androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .keypadFocusable(shape = RoundedCornerShape(10.dp))
                ) {
                    Text(
                        "סגור",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Custom Location Dialog (Compact & Keypad-Friendly)
    if (showCustomLocationDialog) {
        var customName by remember { mutableStateOf("") }
        var customLat by remember { mutableStateOf(state.selectedLocation.latitude.toString()) }
        var customLon by remember { mutableStateOf(state.selectedLocation.longitude.toString()) }
        var customElevation by remember { mutableStateOf(state.selectedLocation.elevationMeters.toString()) }
        var customOffset by remember { mutableStateOf(state.selectedLocation.visibleNetzOffsetMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { showCustomLocationDialog = false },
            title = {
                Text(
                    text = "הגדרת מיקום מותאם אישית",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.5.sp
                    ),
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("שם המיקום", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .keypadFocusable(shape = RoundedCornerShape(8.dp))
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("קו רוחב (Lat)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .keypadFocusable(shape = RoundedCornerShape(8.dp))
                        )
                        OutlinedTextField(
                            value = customLon,
                            onValueChange = { customLon = it },
                            label = { Text("קו אורך (Lon)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .keypadFocusable(shape = RoundedCornerShape(8.dp))
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = customElevation,
                            onValueChange = { customElevation = it },
                            label = { Text("גובה (מ')", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .keypadFocusable(shape = RoundedCornerShape(8.dp))
                        )
                        OutlinedTextField(
                            value = customOffset,
                            onValueChange = { customOffset = it },
                            label = { Text("היסט (דק')", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .keypadFocusable(shape = RoundedCornerShape(8.dp))
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = customLat.toDoubleOrNull() ?: state.selectedLocation.latitude
                        val lon = customLon.toDoubleOrNull() ?: state.selectedLocation.longitude
                        val ele = customElevation.toDoubleOrNull() ?: state.selectedLocation.elevationMeters
                        val off = customOffset.toIntOrNull() ?: 0
                        onSaveCustomLocation(customName, lat, lon, ele, off)
                        showCustomLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = if (colors.isDark) DawnDeepNight else androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .keypadFocusable(shape = RoundedCornerShape(10.dp))
                ) {
                    Text(
                        "שמור והפעל",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCustomLocationDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .height(44.dp)
                        .keypadFocusable(shape = RoundedCornerShape(10.dp))
                ) {
                    Text(
                        "ביטול",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .keypadFocusable(shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp
                ),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.gold,
                checkedTrackColor = colors.gold.copy(alpha = 0.35f),
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
}
