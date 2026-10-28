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
import com.example.ui.theme.LocalAppColors

@Composable
fun DigitalCountdownClock(
    ticker: TickerState,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val borderColor by animateColorAsState(
        targetValue = when {
            ticker.isFinalTenSeconds -> colors.rose
            ticker.isUrgent -> colors.coral
            else -> colors.gold.copy(alpha = 0.40f)
        },
        label = "border_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (ticker.isUrgent) 10.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (ticker.isFinalTenSeconds) colors.rose else colors.gold
            )
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceCard)
            .border(
                width = if (ticker.isUrgent) 2.dp else 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(vertical = 14.dp, horizontal = 4.dp),
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
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = if (isSmall) 2.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .width(if (isSmall) 52.dp else 66.dp)
                .height(if (isSmall) 56.dp else 68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (highlight) colors.rose.copy(alpha = 0.18f)
                    else colors.surfaceVariant
                )
                .border(
                    width = 1.dp,
                    color = if (highlight) colors.rose.copy(alpha = 0.6f) else colors.border,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isSmall) 25.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = when {
                    highlight -> colors.rose
                    isSmall -> colors.coral
                    colors.isDark -> colors.goldLight
                    else -> colors.gold
                },
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (highlight) colors.rose else colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColonSeparator(isUrgent: Boolean) {
    val colors = LocalAppColors.current
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) colors.coral else colors.gold.copy(alpha = 0.6f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .offset(y = (-8).dp)
    )
}

@Composable
private fun DotSeparator(isUrgent: Boolean) {
    val colors = LocalAppColors.current
    Text(
        text = ".",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) colors.rose else colors.gold.copy(alpha = 0.6f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .offset(y = (-8).dp)
    )
}
