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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.model.AppThemeMode
import com.example.model.LocationData
import com.example.model.PredefinedLocations
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
    onSaveCustomLocation: (name: String, lat: Double, lon: Double, elevation: Double, offset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Theme Mode Section (מצב יום / מצב לילה)
        item {
            Text(
                text = "עיצוב: מצב יום / לילה",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AppThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSetThemeMode(mode) }
                                .focusable()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = SunriseGold)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mode.titleHebrew,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (state.themeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 17.sp
                                ),
                                color = if (state.themeMode == mode) SunriseSunGlow else TextWhite
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
                    fontSize = 19.sp
                ),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseGold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "מיקום פעיל:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "קואורדינטות: ${String.format("%.4f", state.selectedLocation.latitude)}°N, ${String.format("%.4f", state.selectedLocation.longitude)}°E | גובה: ${state.selectedLocation.elevationMeters.toInt()} מ'",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = SunriseLightGold.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // שורת כפתורים 1: שתי פעולות מהירות בגובה 54dp שווה
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showCityDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SunriseGold,
                                contentColor = DawnDeepNight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .focusable()
                                .testTag("choose_city_btn")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DawnDeepNight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "בחר עיר",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
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
                                .height(54.dp)
                                .focusable()
                                .testTag("gps_detect_btn"),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseGold.copy(alpha = 0.7f))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = SunriseGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isLocating) "מאתר..." else "זיהוי GPS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = SunriseGold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // שורת כפתורים 2: הגדרה ידנית ברוחב מלא ובגובה 54dp זהה
                    OutlinedButton(
                        onClick = { showCustomLocationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .focusable()
                            .testTag("custom_location_btn"),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocation,
                                contentDescription = null,
                                tint = SunriseSunGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "הגדרת מיקום מותאם אישית",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = TextWhite,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    if (state.locationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.locationError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SunriseRose
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
                    fontSize = 19.sp
                ),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetPlainNetz(false) }
                            .focusable()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !state.isPlainNetz,
                            onClick = { onSetPlainNetz(false) },
                            colors = RadioButtonDefaults.colors(selectedColor = SunriseGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "נץ נראה (טופוגרפי) - מומלץ לוותיקין",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "מחשב את הרגע בו גוף השמש עולה בפועל מעל קו ההרים המקומיים (כפי שנקבע בלוח ארץ ישראל).",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetPlainNetz(true) }
                            .focusable()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.isPlainNetz,
                            onClick = { onSetPlainNetz(true) },
                            colors = RadioButtonDefaults.colors(selectedColor = SunriseGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "נץ מישורי (אסטרונומי)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "זריחה אסטרונומית ישרה בגובה פני הים ללא התחשבות במחסומי הרים מזרחיים.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Sound & Haptic Alerts Section
        item {
            Text(
                text = "צלילים והתראות רטט",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "הפעלת צלילי התראה",
                        subtitle = "צלצול צפצוף ברגע הנץ ובנקודות ציון בתפילה",
                        checked = state.soundEnabled,
                        onCheckedChange = { onToggleSound() }
                    )

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 10.dp))

                    SettingToggleRow(
                        title = "רטט (Haptics)",
                        subtitle = "רטט שקט בעשר השניות וברגע הנץ",
                        checked = state.vibrationEnabled,
                        onCheckedChange = { onToggleVibration() }
                    )

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 10.dp))

                    SettingToggleRow(
                        title = "צפצוף בעשר השניות האחרונות",
                        subtitle = "ספירה קולית (10..9..8..1) לתזמון סיום גאל ישראל",
                        checked = state.tenSecBeepEnabled,
                        onCheckedChange = { onToggleTenSecBeep() }
                    )
                }
            }
        }

        // Keypad Phone Support Guide
        item {
            Surface(
                color = DawnSurfaceVariant,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = SunriseGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "תמיכה במכשירי מקשים (כשרים):",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = SunriseSunGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• כפתורים * ו-# למעבר מהיר בין הלשוניות\n" +
                                "• ספרות 1 עד 5 למעבר ישיר למסכים\n" +
                                "• ספרה 7 להחלפת מצב יום / לילה\n" +
                                "• ספרה 8 להחלפת נץ נראה / מישורי\n" +
                                "• ספרה 0 להשתקת צלילים\n" +
                                "• חצי D-Pad ומקש Enter לבחירה וגלילה",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp
                        ),
                        color = TextWhite
                    )
                }
            }
        }

        item {
            Text(
                text = "גרסה 1.2 • נץ החמה וזמני היום • תצוגה מוגדלת ומהירה",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextMuted.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // City Selection Dialog (Large Items)
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "בחר עיר או יישוב",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    ),
                    color = SunriseSunGlow
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(PredefinedLocations.CITIES) { city ->
                        val isSelected = state.selectedLocation.id == city.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectLocation(city)
                                    showCityDialog = false
                                }
                                .focusable()
                                .padding(vertical = 12.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.nameHebrew,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = if (isSelected) SunriseSunGlow else TextWhite
                                )
                                Text(
                                    text = "${city.nameEnglish} • ${city.elevationMeters.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SunriseGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCityDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SunriseGold, contentColor = DawnDeepNight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        "סגור",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            containerColor = DawnSurfaceCard,
            shape = RoundedCornerShape(22.dp)
        )
    }

    // Custom Location Dialog (Large Inputs)
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
                        fontSize = 20.sp
                    ),
                    color = SunriseSunGlow
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("שם המיקום (למשל: בית כנסת ותיקין)", fontSize = 15.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("קו רוחב (Lat)", fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customLon,
                            onValueChange = { customLon = it },
                            label = { Text("קו אורך (Lon)", fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customElevation,
                            onValueChange = { customElevation = it },
                            label = { Text("גובה במטרים", fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customOffset,
                            onValueChange = { customOffset = it },
                            label = { Text("היסט נץ (דק')", fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
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
                    colors = ButtonDefaults.buttonColors(containerColor = SunriseGold, contentColor = DawnDeepNight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        "שמור והפעל",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCustomLocationDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        "ביטול",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            containerColor = DawnSurfaceCard,
            shape = RoundedCornerShape(22.dp)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SunriseGold,
                checkedTrackColor = SunriseGold.copy(alpha = 0.35f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DawnSurfaceVariant
            )
        )
    }
}
