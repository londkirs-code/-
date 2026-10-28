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
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ==========================================
        // 1. שורת בקרה עליונה: בחירת עיר + כפתור יום/לילה + כפתור צליל (גובה 50dp מרווח ומיושר)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // כפתור בחירת עיר ומיקום
            Surface(
                onClick = onOpenCitySelector,
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
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
                        tint = colors.gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // כפתור מצב יום / לילה (50x50dp)
            Surface(
                onClick = onToggleTheme,
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier
                    .size(50.dp)
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
                    .testTag("theme_toggle_btn")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.themeMode == AppThemeMode.LIGHT) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "החלף מצב יום/לילה",
                        tint = colors.gold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // כפתור צליל והשתקה (50x50dp)
            Surface(
                onClick = onToggleSound,
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier
                    .size(50.dp)
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
                    .testTag("sound_toggle_btn")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.soundEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = if (state.soundEnabled) "צלילים מופעלים" else "צלילים מושתקים",
                        tint = if (state.soundEnabled) colors.gold else colors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. מתג סוג נץ: נץ נראה מול נץ מישורי (גובה 50dp מרווח, מופרד ללא חפיפה)
        // ==========================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.surfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // מקטע 1: נץ נראה
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!state.isPlainNetz) colors.gold.copy(alpha = 0.20f) else Color.Transparent)
                        .border(
                            1.2.dp,
                            if (!state.isPlainNetz) colors.gold else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onTogglePlainNetz(false) }
                        .keypadFocusable(shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "נץ נראה",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = if (!state.isPlainNetz) colors.gold else colors.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(טופוגרפי)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (!state.isPlainNetz) colors.goldLight else colors.textSecondary.copy(alpha = 0.7f),
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (state.isPlainNetz) colors.gold.copy(alpha = 0.20f) else Color.Transparent)
                        .border(
                            1.2.dp,
                            if (state.isPlainNetz) colors.gold else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onTogglePlainNetz(true) }
                        .keypadFocusable(shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "נץ מישורי",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = if (state.isPlainNetz) colors.gold else colors.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(אסטרונומי)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (state.isPlainNetz) colors.goldLight else colors.textSecondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. תאריך עברי מלא (גובה 42dp, 100% ממורכז)
        // ==========================================
        state.currentHebrewDate?.let { hDate ->
            Surface(
                color = colors.gold.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.gold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = hDate.formatHebrew(includeDayOfWeek = true, dayOfWeek = java.time.LocalDate.now().dayOfWeek),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colors.textGold,
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
            color = colors.surfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp),
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "שעת הנץ: ${state.targetNetzTimeFormatted}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = colors.gold,
                textAlign = TextAlign.Center
            )
        }

        // שעון הספירה (ממורכז)
        DigitalCountdownClock(ticker = ticker)

        // הודעת נץ של היום עבר (אם רלוונטי)
        if (state.isTomorrowNetz && state.todayZmanim != null) {
            val todayNetzTime = if (state.isPlainNetz) state.todayZmanim.netzPlain else state.todayZmanim.netzVisible
            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "נץ החמה של היום חל בשעה ${todayNetzTime.format(shortTimeFormatter)}. הספירה כעת לנץ מחר.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // ==========================================
        // 6. כפתורי פעולה ראשיים (גובה 52dp מרווח ומדויק, לעולם לא חתוך!)
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // כפתור 1: סדר תפילת ותיקין (גובה 52dp, מסגרת פוקוס)
            Button(
                onClick = onNavigateToGuide,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.gold,
                    contentColor = if (colors.isDark) DawnDeepNight else Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
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
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "סדר תפילת ותיקין",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // כפתור 2: מצפן כיוון ירושלים (בדיוק אותו גובה 52dp שווה, מסגרת פוקוס)
            OutlinedButton(
                onClick = onNavigateToCompass,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.gold
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.3.dp, colors.gold.copy(alpha = 0.85f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
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
                        tint = colors.gold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "מצפן כיוון ירושלים",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = colors.textPrimary,
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
                color = colors.surfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    if (step.isCrucialMoment) colors.rose else colors.gold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGuide() }
                    .keypadFocusable(shape = RoundedCornerShape(14.dp))
                    .testTag("active_vatikin_step_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "שלב התפילה כעת (ותיקין)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = colors.coral
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (step.isCrucialMoment) colors.rose.copy(alpha = 0.20f) else colors.gold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${step.minutesBeforeNetz} דק' לנץ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (step.isCrucialMoment) colors.rose else colors.gold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }

        // ==========================================
        // 8. ארבעת זמני הבוקר (כרטיסים קומפקטיים בגובה 84dp, ממורכזים ללא חיתוך!)
        // ==========================================
        state.todayZmanim?.let { z ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "זמני הבוקר העיקריים להיום",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = colors.gold,
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZmanQuickCard(
                        title = "עלות השחר",
                        time = z.alotHaShachar16Degrees.format(shortTimeFormatter),
                        subtitle = "16.1°",
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                    )

                    ZmanQuickCard(
                        title = "משיכיר",
                        time = z.misheyakir.format(shortTimeFormatter),
                        subtitle = "תפילין",
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                    )

                    ZmanQuickCard(
                        title = if (state.isPlainNetz) "נץ מישורי" else "נץ נראה",
                        time = (if (state.isPlainNetz) z.netzPlain else z.netzVisible).format(shortTimeFormatter),
                        subtitle = "עמידה",
                        isHighlight = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                    )

                    ZmanQuickCard(
                        title = "סוף ק״ש",
                        time = z.sofZmanShemaGra.format(shortTimeFormatter),
                        subtitle = "הגר״א",
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
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
    val colors = LocalAppColors.current

    Surface(
        color = if (isHighlight) colors.gold.copy(alpha = 0.18f) else colors.surfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isHighlight) colors.gold else colors.border
        ),
        modifier = modifier.keypadFocusable(shape = RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isHighlight) colors.gold else colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp
                ),
                color = if (isHighlight) colors.gold else colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = if (isHighlight) colors.coral else colors.textSecondary.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
