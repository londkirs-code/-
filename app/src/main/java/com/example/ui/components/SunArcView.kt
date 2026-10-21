package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SunriseCoral
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseLightGold
import com.example.ui.theme.SunriseRose
import com.example.ui.theme.SunriseSunGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunArcView(
    sunAltitudeDegrees: Double,
    isNetzReached: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_rotation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val horizonY = height * 0.72f

                // Sky twilight/dawn gradient glow above horizon
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SunriseRose.copy(alpha = 0.12f),
                            SunriseGold.copy(alpha = 0.22f)
                        ),
                        startY = 0f,
                        endY = horizonY
                    ),
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(width, horizonY)
                )

                // Horizon Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            SunriseGold.copy(alpha = 0.1f),
                            SunriseGold.copy(alpha = 0.9f),
                            SunriseLightGold,
                            SunriseGold.copy(alpha = 0.9f),
                            SunriseGold.copy(alpha = 0.1f)
                        )
                    ),
                    start = Offset(16.dp.toPx(), horizonY),
                    end = Offset(width - 16.dp.toPx(), horizonY),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Arc path for the sun
                // Altitude ranges typically from -20° (night) to 0° (horizon/sunrise) to +15° (morning)
                val clampedAlt = sunAltitudeDegrees.coerceIn(-18.0, 10.0)
                // Map clampedAlt to normalized value [0.0 (night) to 1.0 (morning)]
                val progress = ((clampedAlt + 18.0) / 28.0).toFloat().coerceIn(0f, 1f)

                // Sun Arc
                val arcRadiusX = width * 0.42f
                val arcRadiusY = height * 0.58f
                val centerX = width * 0.5f
                val arcCenterY = horizonY

                // Sun position along arc: from angle PI (left) to 0 (right)
                // Hebrew RTL or Eastward: Let's move the sun up towards the horizon
                // At 0° altitude, sun should be exactly at horizonY
                val sunX = centerX - arcRadiusX * cos(progress * PI.toFloat())
                val sunY = arcCenterY - arcRadiusY * sin(progress * PI.toFloat()) * (if (clampedAlt < 0) -0.5f else 0.7f)
                val finalSunY = (horizonY - (clampedAlt * 3.5).toFloat().coerceIn(-40f, 40f)).coerceIn(15.dp.toPx(), height - 15.dp.toPx())

                // Draw dashed guide trajectory
                val guidePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(16.dp.toPx(), horizonY + 25.dp.toPx())
                    quadraticTo(
                        centerX,
                        horizonY - arcRadiusY * 0.8f,
                        width - 16.dp.toPx(),
                        horizonY + 25.dp.toPx()
                    )
                }
                drawPath(
                    path = guidePath,
                    color = SunriseGold.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
                    )
                )

                // Outer atmospheric radiant glow around sun
                val sunGlowRadius = 38.dp.toPx() * (if (isNetzReached) pulseScale * 1.3f else pulseScale)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SunriseSunGlow.copy(alpha = 0.75f),
                            SunriseGold.copy(alpha = 0.4f),
                            SunriseCoral.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(sunX, finalSunY),
                        radius = sunGlowRadius
                    ),
                    radius = sunGlowRadius,
                    center = Offset(sunX, finalSunY)
                )

                // Sun rays if close to or past netz
                if (clampedAlt >= -4.0) {
                    val rayCount = 12
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount) + rayRotation) * (PI.toFloat() / 180f)
                        val r1 = 14.dp.toPx()
                        val r2 = 28.dp.toPx() * pulseScale
                        val startRay = Offset(sunX + r1 * cos(angle), finalSunY + r1 * sin(angle))
                        val endRay = Offset(sunX + r2 * cos(angle), finalSunY + r2 * sin(angle))
                        drawLine(
                            color = SunriseSunGlow.copy(alpha = 0.6f),
                            start = startRay,
                            end = endRay,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Core Sun Orb
                val coreRadius = 13.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            SunriseLightGold,
                            SunriseGold
                        ),
                        center = Offset(sunX - 2.dp.toPx(), finalSunY - 2.dp.toPx()),
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = Offset(sunX, finalSunY)
                )
            }
        }

        // Horizon labels & altitude indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "קו האופק (0.0°)",
                style = MaterialTheme.typography.labelSmall,
                color = SunriseLightGold.copy(alpha = 0.8f)
            )

            Text(
                text = String.format("גובה השמש: %.1f°", sunAltitudeDegrees),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = if (sunAltitudeDegrees >= 0.0) SunriseSunGlow else SunriseCoral
            )
        }
    }
}
