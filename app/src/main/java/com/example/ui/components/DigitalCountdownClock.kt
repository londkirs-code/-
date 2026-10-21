package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DawnSurfaceCard
import com.example.ui.theme.SunriseCoral
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseLightGold
import com.example.ui.theme.SunriseRose
import com.example.ui.theme.SunriseSunGlow

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun DigitalCountdownClock(
    hours: Long,
    minutes: Long,
    seconds: Long,
    centiseconds: Long,
    isUrgent: Boolean, // < 60 seconds
    isFinalTenSeconds: Boolean, // 1..10 seconds
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "clock_urgency")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isFinalTenSeconds) 0.98f else 1.0f,
        targetValue = if (isFinalTenSeconds) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFinalTenSeconds -> SunriseRose
            isUrgent -> SunriseCoral
            else -> SunriseGold.copy(alpha = 0.35f)
        },
        label = "border_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (isFinalTenSeconds) pulseScale else 1.0f)
            .shadow(
                elevation = if (isUrgent) 18.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isFinalTenSeconds) SunriseRose else SunriseGold
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        DawnSurfaceCard,
                        DawnSurfaceCard.copy(alpha = 0.88f)
                    )
                )
            )
            .border(
                width = if (isUrgent) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 20.dp, horizontal = 12.dp),
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
                        value = String.format("%02d", hours),
                        label = "שעות",
                        isUrgent = isUrgent
                    )

                    ColonSeparator(isUrgent = isUrgent)

                    // Minutes (Center)
                    TimeSegment(
                        value = String.format("%02d", minutes),
                        label = "דקות",
                        isUrgent = isUrgent
                    )

                    ColonSeparator(isUrgent = isUrgent)

                    // Seconds
                    TimeSegment(
                        value = String.format("%02d", seconds),
                        label = "שניות",
                        isUrgent = isUrgent,
                        highlight = isUrgent
                    )

                    DotSeparator(isUrgent = isUrgent)

                    // Tenths / Centiseconds (Right)
                    TimeSegment(
                        value = String.format("%02d", centiseconds),
                        label = "מאיות",
                        isUrgent = isUrgent,
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
        modifier = Modifier.padding(horizontal = if (isSmall) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(if (isSmall) 52.dp else 68.dp)
                .height(if (isSmall) 56.dp else 68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (highlight) SunriseRose.copy(alpha = 0.2f)
                    else SunriseGold.copy(alpha = 0.08f)
                )
                .border(
                    width = 1.dp,
                    color = if (highlight) SunriseRose.copy(alpha = 0.5f) else SunriseGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isSmall) 28.sp else 38.sp,
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (highlight) SunriseRose else SunriseLightGold.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColonSeparator(isUrgent: Boolean) {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) SunriseCoral else SunriseGold.copy(alpha = 0.5f),
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
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        ),
        color = if (isUrgent) SunriseRose else SunriseGold.copy(alpha = 0.5f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .offset(y = (-10).dp)
    )
}
