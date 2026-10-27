package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compass.CompassState
import com.example.ui.CountdownUiState
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JerusalemCompassScreen(
    state: CountdownUiState,
    compassState: CompassState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        onStartListening()
        onDispose {
            onStopListening()
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = compassState.deviceAzimuthDegrees,
        animationSpec = tween(durationMillis = 150),
        label = "azimuth_anim"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (compassState.isFacingJerusalem) SunriseSunGlow else DawnBorder,
        label = "compass_border_color"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "מצפן תפילה לירושלים",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = SunriseSunGlow
        )

        Text(
            text = "כיוון התפילה לעבר בית המקדש והכותל המערבי בירושלים",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Status Card: Are we facing Jerusalem?
        Surface(
            color = if (compassState.isFacingJerusalem) SunriseGold.copy(alpha = 0.22f) else DawnSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (compassState.isFacingJerusalem) SunriseSunGlow else DawnBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("compass_status_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (compassState.isFacingJerusalem) Icons.Default.CheckCircle else Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (compassState.isFacingJerusalem) SunriseSunGlow else SunriseGold,
                    modifier = Modifier.size(34.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (compassState.isFacingJerusalem) "פונה במדויק לירושלים!" else "כוון את המכשיר לירושלים",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = if (compassState.isFacingJerusalem) SunriseSunGlow else TextWhite
                    )

                    val angleDiff = compassState.relativeAngleToJerusalemDegrees
                    val instruction = when {
                        compassState.isFacingJerusalem -> "עת לעמוד בתפילת שמונה עשרה"
                        angleDiff > 0 -> "סובב ימינה ב-${angleDiff.toInt()}°"
                        else -> "סובב שמאלה ב-${(-angleDiff).toInt()}°"
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = if (compassState.isFacingJerusalem) SunriseLightGold else SunriseCoral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Compass Visual Dial
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(DawnSurfaceCard)
                .border(2.5.dp, dialBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val radius = w / 2f - 16.dp.toPx()

                // Dial rotation rotates opposite to device azimuth
                val dialRotation = -animatedAzimuth

                // Draw outer degree marks
                for (angle in 0 until 360 step 15) {
                    val rad = (angle + dialRotation) * (PI / 180.0)
                    val isMajor = angle % 90 == 0
                    val isMedium = angle % 45 == 0
                    val tickLen = if (isMajor) 14.dp.toPx() else if (isMedium) 10.dp.toPx() else 6.dp.toPx()

                    val startX = center.x + (radius - tickLen) * sin(rad).toFloat()
                    val startY = center.y - (radius - tickLen) * cos(rad).toFloat()
                    val endX = center.x + radius * sin(rad).toFloat()
                    val endY = center.y - radius * cos(rad).toFloat()

                    drawLine(
                        color = if (isMajor) SunriseLightGold else SunriseGold.copy(alpha = 0.35f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()
                    )
                }

                // Draw cardinal directions: צ (North), מז (East), ד (South), מע (West)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 34f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val cardinals = listOf(
                    0 to "צ",
                    90 to "מז",
                    180 to "ד",
                    270 to "מע"
                )

                for ((ang, text) in cardinals) {
                    val rad = (ang + dialRotation) * (PI / 180.0)
                    val tx = center.x + (radius - 28.dp.toPx()) * sin(rad).toFloat()
                    val ty = center.y - (radius - 28.dp.toPx()) * cos(rad).toFloat() + 12f
                    drawContext.canvas.nativeCanvas.drawText(text, tx, ty, paint)
                }

                // Draw Jerusalem Pointer Line & Emblem
                val jerusalemRad = (compassState.bearingToJerusalemDegrees + dialRotation) * (PI / 180.0)
                val pointerLen = radius - 12.dp.toPx()
                val jerusalemX = center.x + pointerLen * sin(jerusalemRad).toFloat()
                val jerusalemY = center.y - pointerLen * cos(jerusalemRad).toFloat()

                // Radiant golden beam pointing towards Jerusalem
                drawLine(
                    brush = Brush.radialGradient(
                        colors = listOf(SunriseSunGlow, SunriseGold, Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    start = center,
                    end = Offset(jerusalemX, jerusalemY),
                    strokeWidth = 3.dp.toPx()
                )

                // Golden Jerusalem Node
                drawCircle(
                    color = if (compassState.isFacingJerusalem) SunriseSunGlow else SunriseGold,
                    radius = 9.dp.toPx(),
                    center = Offset(jerusalemX, jerusalemY)
                )

                // Center pivot ring
                drawCircle(
                    color = SunriseGold,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = DawnDeepNight,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }

            // Top Device Heading Arrow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (compassState.isFacingJerusalem) SunriseSunGlow else SunriseCoral,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Angle and Distance Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("כיוון ירושלים", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${compassState.bearingToJerusalemDegrees.toInt()}°",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SunriseLightGold
                    )
                }
            }

            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("מרחק מירושלים", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${compassState.distanceToJerusalemKm} ק״מ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SunriseLightGold
                    )
                }
            }

            Surface(
                color = DawnSurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("מיקום נוכחי", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Halachic Sources Card
        Surface(
            color = DawnSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "מקור הדין לכיוון התפילה (שו״ע או״ח סימן צ״ד):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SunriseLightGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "״היה עומד בחוץ לארץ, יחזיר פניו כנגד ארץ ישראל ויכוון גם לירושלים ולבית המקדש ולבית קדשי הקדשים. היה עומד בארץ ישראל, יחזיר פניו כנגד ירושלים. היה עומד בירושלים, יחזיר פניו כנגד המקדש.״ (מבוסס על תפילת שלמה המלך במלכים א' ח').",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = TextWhite
                )
            }
        }
    }
}
