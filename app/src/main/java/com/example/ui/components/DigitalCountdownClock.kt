package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TickerState
import com.example.ui.theme.DawnSurfaceCard
import com.example.ui.theme.SunriseCoral
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseLightGold
import com.example.ui.theme.SunriseRose
import com.example.ui.theme.SunriseSunGlow

@Composable
fun DigitalCountdownClock(
    ticker: TickerState,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            ticker.isFinalTenSeconds -> SunriseRose
            ticker.isUrgent -> SunriseCoral
            else -> SunriseGold.copy(alpha = 0.45f)
        },
        label = "border_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (ticker.isUrgent) 14.dp else 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = if (ticker.isFinalTenSeconds) SunriseRose else SunriseGold
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        DawnSurfaceCard,
                        DawnSurfaceCard.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = if (ticker.isUrgent) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(26.dp)
            )
            .padding(vertical = 20.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Force Left-to-Right layout direction so Hours appear on the left, Minutes in the center, Seconds and Centiseconds on the right
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours (Left)
                    TimeSegment(
                        value = String.format("%02d", ticker.hours),
                        label = "שעות",
                        isUrgent = ticker.isUrgent
                    )

                    ColonSeparator(isUrgent = ticker.isUrgent)

                    // Minutes (Center)
                    TimeSegment(
                        value = String.format("%02d", ticker.minutes),
                        label = "דקות",
                        isUrgent = ticker.isUrgent
                    )

                    ColonSeparator(isUrgent = ticker.isUrgent)

                    // Seconds
                    TimeSegment(
                        value = String.format("%02d", ticker.seconds),
                        label = "שניות",
                        isUrgent = ticker.isUrgent,
                        highlight = ticker.isUrgent
                    )

                    DotSeparator(isUrgent = ticker.isUrgent)

                    // Tenths / Centiseconds (Right)
                    TimeSegment(
                        value = String.format("%02d", ticker.centiseconds),
                        label = "מאיות",
                        isUrgent = ticker.isUrgent,
                        isSmall = true
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSegment(
    value: String,
    label: String,
    isUrgent: Boolean,
    highlight: Boolean = false,
    isSmall: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = if (isSmall) 2.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .width(if (isSmall) 56.dp else 72.dp)
                .height(if (isSmall) 60.dp else 74.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (highlight) SunriseRose.copy(alpha = 0.22f)
                    else SunriseGold.copy(alpha = 0.1f)
                )
                .border(
                    width = 1.2.dp,
                    color = if (highlight) SunriseRose.copy(alpha = 0.6f) else SunriseGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isSmall) 28.sp else 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = when {
                    highlight -> SunriseSunGlow
                    isSmall -> SunriseCoral
                    else -> SunriseLightGold
                },
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (highlight) SunriseRose else SunriseLightGold.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColonSeparator(isUrgent: Boolean) {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) SunriseCoral else SunriseGold.copy(alpha = 0.6f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .offset(y = (-10).dp)
    )
}

@Composable
private fun DotSeparator(isUrgent: Boolean) {
    Text(
        text = ".",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) SunriseRose else SunriseGold.copy(alpha = 0.6f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .offset(y = (-10).dp)
    )
}
