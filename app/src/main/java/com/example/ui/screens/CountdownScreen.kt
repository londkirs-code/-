package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.ui.CountdownUiState
import com.example.ui.TickerState
import com.example.ui.components.DigitalCountdownClock
import com.example.ui.components.SunArcView
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun CountdownScreen(
    state: CountdownUiState,
    ticker: TickerState,
    onTogglePlainNetz: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCitySelector: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToCompass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // 1. שורת בקרה עליונה: בחירת עיר + כפתור יום/לילה + כפתור צליל (גובה 54dp שווה)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // כפתור בחירת עיר ומיקום (ממורכז, עם קיצור עודף)
            Surface(
                onClick = onOpenCitySelector,
                shape = RoundedCornerShape(16.dp),
                color = DawnSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .focusable()
                    .testTag("location_selector_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "מיקום",
                        tint = SunriseGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // כפתור מצב יום / לילה (54x54dp, ממורכז)
            Surface(
                onClick = onToggleTheme,
                shape = RoundedCornerShape(16.dp),
                color = DawnSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
                modifier = Modifier
                    .size(54.dp)
                    .focusable()
                    .testTag("theme_toggle_btn")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.themeMode == AppThemeMode.LIGHT) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "החלף מצב יום/לילה",
                        tint = SunriseGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // כפתור צליל והשתקה (54x54dp, ממורכז)
            Surface(
                onClick = onToggleSound,
                shape = RoundedCornerShape(16.dp),
                color = DawnSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
                modifier = Modifier
                    .size(54.dp)
                    .focusable()
                    .testTag("sound_toggle_btn")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.soundEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = if (state.soundEnabled) "צלילים מופעלים" else "צלילים מושתקים",
                        tint = if (state.soundEnabled) SunriseGold else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. מתג סוג נץ: נץ נראה מול נץ מישורי (גובה 56dp, שני מקטעים שווים, שתי שורות ממורכזות ללא חיתוך כלל!)
        // ==========================================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DawnSurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DawnBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // מקטע 1: נץ נראה
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!state.isPlainNetz) SunriseGold.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (!state.isPlainNetz) SunriseGold else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onTogglePlainNetz(false) }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "נץ נראה",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = if (!state.isPlainNetz) SunriseSunGlow else TextMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = "טופוגרפי",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (!state.isPlainNetz) SunriseGold else TextMuted.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // מקטע 2: נץ מישורי
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (state.isPlainNetz) SunriseGold.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (state.isPlainNetz) SunriseGold else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onTogglePlainNetz(true) }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "נץ מישורי",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = if (state.isPlainNetz) SunriseSunGlow else TextMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = "אסטרונומי",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (state.isPlainNetz) SunriseGold else TextMuted.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. תאריך עברי מלא (ברוחב מלא, לעולם לא נחתך, 100% ממורכז)
        // ==========================================
        state.currentHebrewDate?.let { hDate ->
            Surface(
                color = SunriseGold.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, SunriseGold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = hDate.formatHebrew(includeDayOfWeek = true, dayOfWeek = java.time.LocalDate.now().dayOfWeek),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextGold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ==========================================
        // 4. הדמיית קו האופק וגובה השמש
        // ==========================================
        Surface(
            color = DawnSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SunArcView(
                    sunAltitudeDegrees = state.sunAltitudeDegrees,
                    isNetzReached = ticker.remainingMillis == 0L,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // ==========================================
        // 5. כותרת סטטוס ושעון הספירה הדיגיטלי
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val titleText = if (state.isTomorrowNetz) "ספירה לאחור לנץ מחר" else "הזמן שנותר לנץ החמה"

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                ),
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "שעת הנץ: ${state.targetNetzTimeFormatted}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = SunriseLightGold,
                textAlign = TextAlign.Center
            )
        }

        // שעון הספירה (ממורכז)
        DigitalCountdownClock(ticker = ticker)

        // הודעת נץ של היום עבר (אם רלוונטי)
        if (state.isTomorrowNetz && state.todayZmanim != null) {
            val todayNetzTime = if (state.isPlainNetz) state.todayZmanim.netzPlain else state.todayZmanim.netzVisible
            Surface(
                color = DawnSurfaceVariant.copy(alpha = 0.9f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "נץ החמה של היום חל בשעה ${todayNetzTime.format(shortTimeFormatter)}. הספירה כעת לנץ מחר.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // ==========================================
        // 6. כפתורי פעולה ראשיים (גובה 60dp שווה בדיוק, טקסט תמציתי וממורכז ללא חיתוך!)
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // כפתור 1: סדר תפילת ותיקין (טקסט ממוקד, לעולם לא נחתך)
            Button(
                onClick = onNavigateToGuide,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SunriseGold,
                    contentColor = DawnDeepNight
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .focusable()
                    .testTag("open_vatikin_guide_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = DawnDeepNight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "סדר תפילת ותיקין",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = DawnDeepNight,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // כפתור 2: מצפן כיוון ירושלים (בדיוק אותו גובה 60dp וטקסט ממורכז מושלם)
            Button(
                onClick = onNavigateToCompass,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DawnSurfaceCard,
                    contentColor = SunriseSunGlow
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SunriseGold.copy(alpha = 0.8f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .focusable()
                    .testTag("open_jerusalem_compass_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = SunriseSunGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "מצפן כיוון ירושלים",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = SunriseSunGlow,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // ==========================================
        // 7. שלב התפילה כעת (אם רלוונטי)
        // ==========================================
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
                    .clickable { onNavigateToGuide() }
                    .focusable()
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
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = SunriseCoral
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (step.isCrucialMoment) SunriseRose.copy(alpha = 0.25f) else SunriseGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${step.minutesBeforeNetz} דק' לפני הנץ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = if (step.isCrucialMoment) SunriseRose else SunriseGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = TextMuted
                    )
                }
            }
        }

        // ==========================================
        // 8. ארבעת זמני הבוקר (כרטיסים שווים בגובה 96dp, ממורכזים ללא חיתוך!)
        // ==========================================
        state.todayZmanim?.let { z ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "זמני הבוקר העיקריים להיום",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = SunriseLightGold,
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZmanQuickCard(
                        title = "עלות השחר",
                        time = z.alotHaShachar16Degrees.format(shortTimeFormatter),
                        subtitle = "16.1°",
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                    )

                    ZmanQuickCard(
                        title = "משיכיר",
                        time = z.misheyakir.format(shortTimeFormatter),
                        subtitle = "תפילין",
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                    )

                    ZmanQuickCard(
                        title = if (state.isPlainNetz) "נץ מישורי" else "נץ נראה",
                        time = (if (state.isPlainNetz) z.netzPlain else z.netzVisible).format(shortTimeFormatter),
                        subtitle = "עמידה",
                        isHighlight = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                    )

                    ZmanQuickCard(
                        title = "סוף ק״ש",
                        time = z.sofZmanShemaGra.format(shortTimeFormatter),
                        subtitle = "הגר״א",
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                    )
                }
            }
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
        color = if (isHighlight) SunriseGold.copy(alpha = 0.22f) else DawnSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isHighlight) SunriseGold else DawnBorder
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isHighlight) SunriseSunGlow else TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.5.sp
                ),
                color = if (isHighlight) SunriseSunGlow else TextWhite,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = if (isHighlight) SunriseCoral else TextMuted.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
