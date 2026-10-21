package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CountdownUiState
import com.example.ui.components.DigitalCountdownClock
import com.example.ui.components.SunArcView
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun CountdownScreen(
    state: CountdownUiState,
    onTogglePlainNetz: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onOpenCitySelector: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val isUrgent = state.remainingMillis in 1..59_999
    val isFinalTen = state.remainingMillis in 1..10_999

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action & Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City selector button
            Surface(
                onClick = onOpenCitySelector,
                shape = RoundedCornerShape(20.dp),
                color = DawnSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SunriseGold.copy(alpha = 0.3f)),
                modifier = Modifier.testTag("location_selector_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "מיקום",
                        tint = SunriseGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }
            }

            // Right side controls: Netz Type toggle & Sound
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Netz Type Switch Pill
                Surface(
                    onClick = { onTogglePlainNetz(!state.isPlainNetz) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (state.isPlainNetz) DawnSurfaceVariant else SunriseGold.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isPlainNetz) DawnBorder else SunriseGold.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("toggle_netz_type_btn")
                ) {
                    Text(
                        text = if (state.isPlainNetz) "נץ מישורי" else "נץ נראה",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (state.isPlainNetz) TextMuted else SunriseSunGlow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sound toggle button
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DawnSurfaceCard)
                        .border(1.dp, DawnBorder, CircleShape)
                        .testTag("sound_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (state.soundEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = if (state.soundEnabled) "צלילים מופעלים" else "צלילים מושתקים",
                        tint = if (state.soundEnabled) SunriseGold else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Hebrew Date Display
        state.currentHebrewDate?.let { hDate ->
            Surface(
                color = SunriseGold.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunriseGold.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = hDate.formatHebrew(includeDayOfWeek = true, dayOfWeek = state.currentTime.dayOfWeek),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = TextGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sun Arc Horizon Visualization
        SunArcView(
            sunAltitudeDegrees = state.sunAltitudeDegrees,
            isNetzReached = state.remainingMillis == 0L,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Countdown Header / Status Title
        val titleText = if (state.isTomorrowNetz) {
            "ספירה לאחור לנץ מחר"
        } else {
            "הזמן שנותר לנץ החמה"
        }
        val targetTimeString = state.targetNetz.format(timeFormatter)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = SunriseGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "שעת הנץ המשוערת: $targetTimeString",
            style = MaterialTheme.typography.bodyMedium,
            color = SunriseLightGold.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Big Precision Digital Countdown Clock
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            DigitalCountdownClock(
                hours = state.hours,
                minutes = state.minutes,
                seconds = state.seconds,
                centiseconds = state.centiseconds,
                isUrgent = isUrgent,
                isFinalTenSeconds = isFinalTen
            )
        }

        // Passed notice if tomorrow's countdown is active
        if (state.isTomorrowNetz && state.todayZmanim != null) {
            val todayNetzTime = if (state.isPlainNetz) state.todayZmanim.netzPlain else state.todayZmanim.netzVisible
            Surface(
                color = DawnSurfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "נץ החמה של היום חל בשעה ${todayNetzTime.format(shortTimeFormatter)}. הספירה כעת היא לנץ של מחר.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Vatikin Step Guide Card
        state.activeVatikinStep?.let { step ->
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (step.isCrucialMoment) SunriseRose else SunriseGold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToGuide() }
                    .testTag("active_vatikin_step_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "שלב התפילה כעת (ותיקין)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SunriseCoral
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (step.isCrucialMoment) SunriseRose.copy(alpha = 0.2f) else SunriseGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${step.minutesBeforeNetz} דק' לפני הנץ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (step.isCrucialMoment) SunriseRose else SunriseGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.halachicNote,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SunriseLightGold.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Daily Morning Zmanim Quick Bar
        state.todayZmanim?.let { z ->
            Text(
                text = "זמני הבוקר העיקריים להיום",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SunriseLightGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZmanQuickCard(
                    title = "עלות השחר",
                    time = z.alotHaShachar16Degrees.format(shortTimeFormatter),
                    subtitle = "16.1°",
                    modifier = Modifier.weight(1f)
                )

                ZmanQuickCard(
                    title = "משיכיר",
                    time = z.misheyakir.format(shortTimeFormatter),
                    subtitle = "טלית ותפילין",
                    modifier = Modifier.weight(1f)
                )

                ZmanQuickCard(
                    title = if (state.isPlainNetz) "נץ מישורי" else "נץ נראה",
                    time = (if (state.isPlainNetz) z.netzPlain else z.netzVisible).format(shortTimeFormatter),
                    subtitle = "תפילת עמידה",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )

                ZmanQuickCard(
                    title = "סוף זמן שמע",
                    time = z.sofZmanShemaGra.format(shortTimeFormatter),
                    subtitle = "הגר״א",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Vatikin Prayer Guide CTA Banner
        Button(
            onClick = onNavigateToGuide,
            colors = ButtonDefaults.buttonColors(
                containerColor = SunriseGold,
                contentColor = DawnDeepNight
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp)
                .testTag("open_vatikin_guide_btn")
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "צפה בסדר ולוח זמני תפילת ותיקין המלא",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ZmanQuickCard(
    title: String,
    time: String,
    subtitle: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isHighlight) SunriseGold.copy(alpha = 0.15f) else DawnSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlight) SunriseGold else DawnBorder
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isHighlight) SunriseSunGlow else TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = if (isHighlight) SunriseSunGlow else TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isHighlight) SunriseCoral else TextMuted.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
