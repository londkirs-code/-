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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onRotateManual: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    DisposableEffect(Unit) {
        onStartListening()
        onDispose {
            onStopListening()
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = compassState.deviceAzimuthDegrees,
        animationSpec = tween(durationMillis = 100),
        label = "azimuth_anim"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (compassState.isFacingJerusalem) colors.gold else colors.border,
        label = "compass_border_color"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Text(
            text = "מצפן תפילה לירושלים",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = colors.textPrimary
        )

        // Live GPS / Sensor telemetry badge
        Surface(
            color = if (compassState.isGpsLive) colors.gold.copy(alpha = 0.12f) else colors.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (compassState.isGpsLive) colors.gold.copy(alpha = 0.4f) else colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = if (compassState.isGpsLive) colors.gold else colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (compassState.isGpsLive) "GPS פעיל בלייב • ${compassState.headingSource}" else "מקור: ${compassState.headingSource}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        color = if (compassState.isGpsLive) colors.gold else colors.textSecondary
                    )
                }
                Text(
                    text = "${String.format("%.4f", compassState.currentLat)}°N, ${String.format("%.4f", compassState.currentLon)}°E",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                    color = colors.textSecondary
                )
            }
        }

        // Status Card: Are we facing Jerusalem?
        Surface(
            color = if (compassState.isFacingJerusalem) colors.gold.copy(alpha = 0.20f) else colors.surfaceCard,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (compassState.isFacingJerusalem) colors.gold else colors.border
            ),
            modifier = Modifier
                .fillMaxWidth()
                .keypadFocusable(shape = RoundedCornerShape(14.dp))
                .testTag("compass_status_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (compassState.isFacingJerusalem) Icons.Default.CheckCircle else Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (compassState.isFacingJerusalem) colors.gold else colors.coral,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (compassState.isFacingJerusalem) "פונה במדויק לירושלים!" else "כוון את המכשיר לירושלים",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = if (compassState.isFacingJerusalem) colors.gold else colors.textPrimary
                    )

                    val angleDiff = compassState.relativeAngleToJerusalemDegrees
                    val instruction = when {
                        compassState.isFacingJerusalem -> "עת לעמוד בתפילת שמונה עשרה כוותיקין"
                        angleDiff > 0 -> "סובב ימינה ב-${angleDiff.toInt()}°"
                        else -> "סובב שמאלה ב-${(-angleDiff).toInt()}°"
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        ),
                        color = if (compassState.isFacingJerusalem) colors.textGold else colors.coral
                    )
                }
            }
        }

        // Compass Visual Dial
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(colors.surfaceCard)
                .border(2.dp, dialBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val isDark = colors.isDark
            val primaryGold = colors.gold
            val textPrimaryColor = colors.textPrimary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val radius = w / 2f - 14.dp.toPx()

                // Dial rotation rotates opposite to device azimuth
                val dialRotation = -animatedAzimuth

                // Draw outer degree marks
                for (angle in 0 until 360 step 15) {
                    val rad = (angle + dialRotation) * (PI / 180.0)
                    val isMajor = angle % 90 == 0
                    val isMedium = angle % 45 == 0
                    val tickLen = if (isMajor) 12.dp.toPx() else if (isMedium) 8.dp.toPx() else 5.dp.toPx()

                    val startX = center.x + (radius - tickLen) * sin(rad).toFloat()
                    val startY = center.y - (radius - tickLen) * cos(rad).toFloat()
                    val endX = center.x + radius * sin(rad).toFloat()
                    val endY = center.y - radius * cos(rad).toFloat()

                    drawLine(
                        color = if (isMajor) primaryGold else primaryGold.copy(alpha = 0.35f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Draw cardinal directions: צ (North), מז (East), ד (South), מע (West)
                val paint = android.graphics.Paint().apply {
                    color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#0F172A")
                    textSize = 30f
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
                    val tx = center.x + (radius - 24.dp.toPx()) * sin(rad).toFloat()
                    val ty = center.y - (radius - 24.dp.toPx()) * cos(rad).toFloat() + 10f
                    drawContext.canvas.nativeCanvas.drawText(text, tx, ty, paint)
                }

                // Draw Jerusalem Pointer Line & Emblem
                val jerusalemRad = (compassState.bearingToJerusalemDegrees + dialRotation) * (PI / 180.0)
                val pointerLen = radius - 10.dp.toPx()
                val jerusalemX = center.x + pointerLen * sin(jerusalemRad).toFloat()
                val jerusalemY = center.y - pointerLen * cos(jerusalemRad).toFloat()

                // Radiant golden beam pointing towards Jerusalem
                drawLine(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryGold, primaryGold.copy(alpha = 0.7f), Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    start = center,
                    end = Offset(jerusalemX, jerusalemY),
                    strokeWidth = 3.dp.toPx()
                )

                // Golden Jerusalem Node
                drawCircle(
                    color = if (compassState.isFacingJerusalem) primaryGold else primaryGold.copy(alpha = 0.8f),
                    radius = 8.dp.toPx(),
                    center = Offset(jerusalemX, jerusalemY)
                )

                // Center pivot ring
                drawCircle(
                    color = primaryGold,
                    radius = 7.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = if (isDark) DawnDeepNight else Color.White,
                    radius = 3.5.dp.toPx(),
                    center = center
                )
            }

            // Top Device Heading Arrow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (compassState.isFacingJerusalem) colors.gold else colors.coral,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Manual rotation test buttons (height 48dp, centered and comfortable)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onRotateManual(-15f) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .keypadFocusable(shape = RoundedCornerShape(12.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, tint = colors.gold, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("סובב ימינה 15°", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
            }

            OutlinedButton(
                onClick = { onRotateManual(15f) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .keypadFocusable(shape = RoundedCornerShape(12.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RotateLeft, contentDescription = null, tint = colors.gold, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("סובב שמאלה 15°", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
            }
        }

        // Angle and Distance Badges (compact)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .keypadFocusable(shape = RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("כיוון ירושלים", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${compassState.bearingToJerusalemDegrees.toInt()}°",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = colors.gold
                    )
                }
            }

            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .keypadFocusable(shape = RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("מרחק מירושלים", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${compassState.distanceToJerusalemKm} ק״מ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = colors.gold
                    )
                }
            }

            Surface(
                color = colors.surfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .keypadFocusable(shape = RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("מיקום נוכחי", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp), color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.selectedLocation.nameHebrew,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        // Halachic Sources Card
        Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "מקור הדין לכיוון התפילה (שו״ע או״ח סימן צ״ד):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = colors.gold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "״היה עומד בחוץ לארץ, יחזיר פניו כנגד ארץ ישראל ויכוון גם לירושלים ולבית המקדש ולבית קדשי הקדשים. היה עומד בארץ ישראל, יחזיר פניו כנגד ירושלים. היה עומד בירושלים, יחזיר פניו כנגד המקדש.״ (מבוסס על תפילת שלמה המלך במלכים א' ח').",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}
