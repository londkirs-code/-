package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
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
                            SunriseRose.copy(alpha = 0.08f),
                            SunriseGold.copy(alpha = 0.18f)
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
                            SunriseGold.copy(alpha = 0.8f),
                            SunriseLightGold,
                            SunriseGold.copy(alpha = 0.8f),
                            SunriseGold.copy(alpha = 0.1f)
                        )
                    ),
                    start = Offset(16.dp.toPx(), horizonY),
                    end = Offset(width - 16.dp.toPx(), horizonY),
                    strokeWidth = 2.dp.toPx()
                )

                // Arc path for the sun
                val clampedAlt = sunAltitudeDegrees.coerceIn(-18.0, 10.0)
                val progress = ((clampedAlt + 18.0) / 28.0).toFloat().coerceIn(0f, 1f)

                val arcRadiusX = width * 0.40f
                val centerX = width * 0.5f

                val sunX = centerX - arcRadiusX * cos(progress * PI.toFloat())
                val finalSunY = (horizonY - (clampedAlt * 3.2).toFloat().coerceIn(-35f, 35f)).coerceIn(14.dp.toPx(), height - 14.dp.toPx())

                // Guide dashed arc
                val guidePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(16.dp.toPx(), horizonY + 20.dp.toPx())
                    quadraticTo(
                        centerX,
                        horizonY - 45.dp.toPx(),
                        width - 16.dp.toPx(),
                        horizonY + 20.dp.toPx()
                    )
                }
                drawPath(
                    path = guidePath,
                    color = SunriseGold.copy(alpha = 0.2f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
                    )
                )

                // Atmospheric radiant glow around sun
                val sunGlowRadius = if (isNetzReached) 36.dp.toPx() else 28.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SunriseSunGlow.copy(alpha = 0.65f),
                            SunriseGold.copy(alpha = 0.35f),
                            SunriseCoral.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(sunX, finalSunY),
                        radius = sunGlowRadius
                    ),
                    radius = sunGlowRadius,
                    center = Offset(sunX, finalSunY)
                )

                // Sun rays when sun is rising or above horizon
                if (clampedAlt >= -4.0) {
                    val rayCount = 8
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount)) * (PI.toFloat() / 180f)
                        val r1 = 12.dp.toPx()
                        val r2 = 22.dp.toPx()
                        val startRay = Offset(sunX + r1 * cos(angle), finalSunY + r1 * sin(angle))
                        val endRay = Offset(sunX + r2 * cos(angle), finalSunY + r2 * sin(angle))
                        drawLine(
                            color = SunriseSunGlow.copy(alpha = 0.5f),
                            start = startRay,
                            end = endRay,
                            strokeWidth = 1.8.dp.toPx()
                        )
                    }
                }

                // Core Sun Orb
                val coreRadius = 11.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            SunriseLightGold,
                            SunriseGold
                        ),
                        center = Offset(sunX - 1.5.dp.toPx(), finalSunY - 1.5.dp.toPx()),
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
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "קו האופק (0.0°)",
                style = MaterialTheme.typography.labelSmall,
                color = SunriseLightGold.copy(alpha = 0.75f)
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
