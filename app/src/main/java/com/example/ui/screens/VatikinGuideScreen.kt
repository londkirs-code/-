package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VatikinGuideData
import com.example.ui.CountdownUiState
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun VatikinGuideScreen(
    state: CountdownUiState,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val shortFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val netzTime = state.targetNetz

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        item {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunriseGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "סדר תפילת כוותיקין",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SunriseSunGlow
                        )
                        Surface(
                            color = SunriseGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "נץ היעד: ${netzTime.format(timeFormatter)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = SunriseLightGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "לוח זמנים מחושב במדויק לדקה ולשנייה לפי נץ היעד ב${state.selectedLocation.nameHebrew}, כדי להגיע לסמיכת גאולה לתפילה ותחילת שמונה עשרה בדיוק ברגע הזריחה.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Timeline Header
        item {
            Text(
                text = "מהלך התפילה המתוזמן",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SunriseLightGold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Timeline items
        itemsIndexed(VatikinGuideData.STEPS) { index, step ->
            val stepTime = netzTime.minusMinutes(step.minutesBeforeNetz.toLong())
            val isPassed = state.currentTime.isAfter(stepTime)
            val isCurrent = state.activeVatikinStep?.id == step.id

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vatikin_step_$index")
            ) {
                // Time column
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stepTime.format(shortFormatter),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = when {
                            isCurrent -> SunriseSunGlow
                            isPassed -> TextMuted
                            else -> TextGold
                        }
                    )
                    Text(
                        text = if (step.minutesBeforeNetz == 0) "רגע הנץ" else "-${step.minutesBeforeNetz} דק'",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (step.isCrucialMoment) SunriseRose else TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Timeline Line & Node
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCrucialMoment -> SunriseRose
                                    isCurrent -> SunriseGold
                                    isPassed -> SunriseGold.copy(alpha = 0.3f)
                                    else -> DawnBorder
                                }
                            )
                            .border(
                                2.dp,
                                if (isCurrent) SunriseSunGlow else Color.Transparent,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPassed && !isCurrent) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DawnDeepNight,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (step.isCrucialMoment) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    if (index < VatikinGuideData.STEPS.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(64.dp)
                                .background(
                                    if (isPassed) SunriseGold.copy(alpha = 0.4f) else DawnBorder
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Card details
                Surface(
                    color = when {
                        step.isCrucialMoment -> SunriseRose.copy(alpha = 0.12f)
                        isCurrent -> SunriseGold.copy(alpha = 0.15f)
                        else -> DawnSurfaceCard
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            step.isCrucialMoment -> SunriseRose
                            isCurrent -> SunriseGold
                            else -> DawnBorder
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (step.isCrucialMoment) SunriseSunGlow else TextWhite
                            )
                            if (isCurrent) {
                                Surface(
                                    color = SunriseGold,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "עכשיו",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DawnDeepNight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.halachicNote,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SunriseLightGold.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // Halachic Sources Expander
        item {
            Surface(
                color = DawnSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "מקורות ההלכה והמעלה של תפילת ותיקין",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SunriseLightGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = VatikinGuideData.HALACHIC_SOURCES,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextWhite
                    )
                }
            }
        }
    }
}
