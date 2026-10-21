package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location Section
        item {
            Text(
                text = "מיקום וקואורדינטות",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunriseGold.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "מיקום נבחר:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = state.selectedLocation.nameHebrew,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }

                        Button(
                            onClick = { showCityDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SunriseGold, contentColor = DawnDeepNight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("choose_city_btn")
                        ) {
                            Text("החלף עיר", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "קואורדינטות: ${String.format("%.4f", state.selectedLocation.latitude)}°N, ${String.format("%.4f", state.selectedLocation.longitude)}°E | גובה: ${state.selectedLocation.elevationMeters.toInt()} מטר",
                        style = MaterialTheme.typography.bodySmall,
                        color = SunriseLightGold.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier.weight(1f).testTag("gps_detect_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SunriseGold.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = SunriseGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isLocating) "מאתר..." else "זהה GPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = SunriseGold
                            )
                        }

                        OutlinedButton(
                            onClick = { showCustomLocationDialog = true },
                            modifier = Modifier.weight(1f).testTag("custom_location_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder)
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("הגדרה ידנית", style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        }
                    }

                    if (state.locationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.locationError,
                            style = MaterialTheme.typography.bodySmall,
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetPlainNetz(false) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !state.isPlainNetz,
                            onClick = { onSetPlainNetz(false) },
                            colors = RadioButtonDefaults.colors(selectedColor = SunriseGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "נץ נראה (טופוגרפי) - מומלץ לוותיקין",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = "מחשב את הרגע בו גוף השמש עולה בפועל מעל קו ההרים המקומיים (כפי שנקבע בלוח ארץ ישראל).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetPlainNetz(true) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.isPlainNetz,
                            onClick = { onSetPlainNetz(true) },
                            colors = RadioButtonDefaults.colors(selectedColor = SunriseGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "נץ מישורי (אסטרונומי)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = "זריחה אסטרונומית ישרה בגובה פני הים ללא התחשבות במחסומי הרים מזרחיים.",
                                style = MaterialTheme.typography.bodySmall,
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SunriseLightGold
            )
        }

        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "הפעלת צלילי התראה",
                        subtitle = "צלצול צפצוף ברגע הנץ ובנקודות ציון בתפילה",
                        checked = state.soundEnabled,
                        onCheckedChange = { onToggleSound() }
                    )

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "רטט (Haptics)",
                        subtitle = "רטט שקט בעשר השניות וברגע הנץ",
                        checked = state.vibrationEnabled,
                        onCheckedChange = { onToggleVibration() }
                    )

                    HorizontalDivider(color = DawnBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "צפצוף בעשר השניות האחרונות",
                        subtitle = "ספירה קולית (10..9..8..1) כדי לתזמן את סיום גאל ישראל",
                        checked = state.tenSecBeepEnabled,
                        onCheckedChange = { onToggleTenSecBeep() }
                    )
                }
            }
        }

        item {
            Text(
                text = "גרסה 1.0 • מבוסס על אלגוריתמי זריחה מדויקים של NOAA ולוחות הוותיקין",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "בחר עיר או יישוב",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SunriseSunGlow
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(PredefinedLocations.CITIES) { city ->
                        val isSelected = state.selectedLocation.id == city.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLocation(city)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.nameHebrew,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SunriseSunGlow else TextWhite
                                )
                                Text(
                                    text = "${city.nameEnglish} • ${city.elevationMeters.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SunriseGold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("סגור", color = SunriseGold)
                }
            },
            containerColor = DawnSurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Custom Location Dialog
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SunriseSunGlow
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
                        label = { Text("שם המיקום (למשל: בית כנסת ותיקין)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("קו רוחב (Lat)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customLon,
                            onValueChange = { customLon = it },
                            label = { Text("קו אורך (Lon)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customElevation,
                            onValueChange = { customElevation = it },
                            label = { Text("גובה במטרים") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customOffset,
                            onValueChange = { customOffset = it },
                            label = { Text("היסט נץ נראה (דק')") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = SunriseGold, contentColor = DawnDeepNight)
                ) {
                    Text("שמור והפעל", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomLocationDialog = false }) {
                    Text("ביטול", color = TextMuted)
                }
            },
            containerColor = DawnSurfaceCard,
            shape = RoundedCornerShape(20.dp)
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
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SunriseGold,
                checkedTrackColor = SunriseGold.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DawnSurfaceVariant
            )
        )
    }
}
