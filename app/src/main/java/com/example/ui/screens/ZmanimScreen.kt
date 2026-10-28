package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.DayZmanim
import com.example.calendar.HebrewDate
import com.example.ui.CountdownUiState
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ZmanimScreen(
    state: CountdownUiState,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val zmanim = state.selectedDayZmanim
    val hebrewDate = state.selectedDayHebrewDate
    val currentDate = state.selectedZmanimDate
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val shortFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Date Navigation Header
        item {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSelectDate(currentDate.minusDays(1)) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                                .keypadFocusable(shape = CircleShape)
                                .testTag("zmanim_prev_day_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "יום קודם",
                                tint = colors.gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentDate.format(dateFormatter),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = colors.textPrimary
                            )

                            hebrewDate?.let { hDate ->
                                Text(
                                    text = hDate.formatHebrew(includeDayOfWeek = true, dayOfWeek = currentDate.dayOfWeek),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.textGold
                                )
                            }
                        }

                        IconButton(
                            onClick = { onSelectDate(currentDate.plusDays(1)) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                                .keypadFocusable(shape = CircleShape)
                                .testTag("zmanim_next_day_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "יום הבא",
                                tint = colors.gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (currentDate != LocalDate.now()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onSelectDate(LocalDate.now()) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .height(38.dp)
                                .keypadFocusable(shape = RoundedCornerShape(10.dp)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colors.gold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "חזור להיום",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = colors.gold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: זמני הבוקר והנץ
        item {
            Text(
                text = "זמני הבוקר ונץ החמה",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = colors.gold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        if (zmanim != null) {
            item {
                ZmanDetailCard(
                    title = "עלות השחר (16.1°)",
                    description = "תחילת היום ההלכתי. השמש 16.1 מעלות מתחת לאופק.",
                    time = zmanim.alotHaShachar16Degrees.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "עלות השחר (72 דקות)",
                    description = "72 דקות קודם הנץ המישורי (שיטת בעל התניא ועוד).",
                    time = zmanim.alotHaShachar72Min.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "משיכיר (טלית ותפילין)",
                    description = "השמש 10.2 מעלות תחת האופק. ברכה על ציצית ותפילין.",
                    time = zmanim.misheyakir.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "נץ החמה - נץ נראה (טופוגרפי)",
                    description = "זריחה מעל ההרים והאופק הנראה. זמן תפילת ותיקין לפי לוח ארץ ישראל.",
                    time = zmanim.netzVisible.format(timeFormatter),
                    isPrimary = true
                )
            }

            item {
                ZmanDetailCard(
                    title = "נץ החמה - נץ מישורי (אסטרונומי)",
                    description = "זריחת השמש באופק אסטרונומי (גובה פני הים).",
                    time = zmanim.netzPlain.format(timeFormatter),
                    isAccent = true
                )
            }

            // Section: סוף זמן קריאת שמע ותפילה
            item {
                Text(
                    text = "סוף זמן קריאת שמע ותפילה",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = colors.gold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            item {
                ZmanDetailCard(
                    title = "סוף זמן קריאת שמע (מג״א)",
                    description = "3 שעות זמניות מעלות השחר (מגן אברהם).",
                    time = zmanim.sofZmanShemaMGA.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "סוף זמן קריאת שמע (הגר״א ובעל התניא)",
                    description = "3 שעות זמניות מהנץ (הגר״א).",
                    time = zmanim.sofZmanShemaGra.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "סוף זמן תפילה (מג״א)",
                    description = "4 שעות זמניות מעלות השחר.",
                    time = zmanim.sofZmanTefillahMGA.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "סוף זמן תפילה (הגר״א)",
                    description = "4 שעות זמניות מהנץ.",
                    time = zmanim.sofZmanTefillahGra.format(timeFormatter)
                )
            }

            // Section: שאר זמני היום
            item {
                Text(
                    text = "זמני המשך היום",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = colors.gold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            item {
                ZmanDetailCard(
                    title = "חצות היום (Solar Noon)",
                    description = "השמש בשיא גובהה בדרום.",
                    time = zmanim.chatzot.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "מנחה גדולה",
                    description = "חצי שעה זמנית אחר חצות.",
                    time = zmanim.minchaGedola.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "פלג המנחה",
                    description = "שעה ורבע זמנית לפני השקיעה.",
                    time = zmanim.plagHaMincha.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "שקיעת החמה",
                    description = "סוף זמן תפילת מנחה ותחילת בין השמשות.",
                    time = zmanim.shkia.format(timeFormatter)
                )
            }

            item {
                ZmanDetailCard(
                    title = "צאת הכוכבים",
                    description = "יציאת 3 כוכבים בינוניים (לילה ודאי).",
                    time = zmanim.tzeitHaKochavim.format(timeFormatter)
                )
            }

            // Shaot Zmaniyot card
            item {
                val graMinutes = zmanim.shaahZmanitGraMs / 60000
                val graSeconds = (zmanim.shaahZmanitGraMs % 60000) / 1000
                val mgaMinutes = zmanim.shaahZmanitMGAMs / 60000
                val mgaSeconds = (zmanim.shaahZmanitMGAMs % 60000) / 1000

                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "אורך שעה זמנית ליום זה:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = colors.gold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "הגר״א: ${graMinutes} דק' ו-${graSeconds} שנ'",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "מג״א: ${mgaMinutes} דק' ו-${mgaSeconds} שנ'",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZmanDetailCard(
    title: String,
    description: String,
    time: String,
    isPrimary: Boolean = false,
    isAccent: Boolean = false
) {
    val colors = LocalAppColors.current

    Surface(
        color = when {
            isPrimary -> colors.gold.copy(alpha = 0.16f)
            isAccent -> colors.coral.copy(alpha = 0.12f)
            else -> colors.surfaceCard
        },
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            when {
                isPrimary -> colors.gold
                isAccent -> colors.coral.copy(alpha = 0.7f)
                else -> colors.border
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .keypadFocusable(shape = RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = if (isPrimary) colors.gold else colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isPrimary) colors.gold else colors.surfaceVariant
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.5.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = if (isPrimary) (if (colors.isDark) DawnDeepNight else androidx.compose.ui.graphics.Color.White) else colors.textGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
