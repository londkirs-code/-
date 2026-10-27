package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.*

@Composable
fun TefillinMirrorScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var showGuidelines by remember { mutableStateOf(true) }
    var screenFillLight by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (screenFillLight) Color.White.copy(alpha = 0.95f) else DawnDeepNight)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "מראת תפילין מכוונת",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (screenFillLight) Color.Black else SunriseSunGlow
        )

        Text(
            text = "ודא שהתפילין מונחות בדיוק בין העיניים ומעל שורש השערות",
            style = MaterialTheme.typography.bodySmall,
            color = if (screenFillLight) Color.DarkGray else TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Camera Preview Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = if (screenFillLight) 4.dp else 2.dp,
                    color = if (screenFillLight) SunriseGold else DawnBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // CameraX Preview
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val cameraSelector = if (useFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (e: Exception) {
                                // Fallback to back camera or log
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Guidelines Overlay
                if (showGuidelines) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val centerX = w / 2f

                        // 1. Vertical Center Line (בין העיניים)
                        drawLine(
                            color = SunriseSunGlow.copy(alpha = 0.85f),
                            start = Offset(centerX, 20f),
                            end = Offset(centerX, h - 20f),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )

                        // 2. Hairline Reference (שורש השערות)
                        val hairlineY = h * 0.35f
                        drawLine(
                            color = SunriseCoral.copy(alpha = 0.8f),
                            start = Offset(w * 0.18f, hairlineY),
                            end = Offset(w * 0.82f, hairlineY),
                            strokeWidth = 2.dp.toPx()
                        )

                        // 3. Upper Boundary (סוף הפדחת / מוחו של תינוק)
                        val upperY = h * 0.20f
                        drawLine(
                            color = SunriseGold.copy(alpha = 0.5f),
                            start = Offset(w * 0.25f, upperY),
                            end = Offset(w * 0.75f, upperY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        // 4. Center Crosshairs Target Box for Tefillin Bayit
                        val boxWidth = 80.dp.toPx()
                        val boxHeight = 70.dp.toPx()
                        val boxTop = upperY + 5.dp.toPx()
                        drawRect(
                            color = SunriseSunGlow.copy(alpha = 0.35f),
                            topLeft = Offset(centerX - boxWidth / 2f, boxTop),
                            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        )
                    }
                }
            } else {
                // Request Permission State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = null,
                        tint = SunriseGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "נדרשת הרשאת מצלמה",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ההרשאה משמשת כמראה מקומית על המכשיר כדי לוודא את דיוק הנחת התפילין. שום תמונה אינה נשמרת או משודרת.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = SunriseGold, contentColor = DawnDeepNight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.focusable().testTag("grant_camera_perm_btn")
                    ) {
                        Text("אפשר גישה למצלמה", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Control Buttons (with Keypad shortcuts hints)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle Guidelines
            OutlinedButton(
                onClick = { showGuidelines = !showGuidelines },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (showGuidelines) SunriseGold else DawnBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (showGuidelines) SunriseGold.copy(alpha = 0.15f) else Color.Transparent
                ),
                modifier = Modifier.focusable().testTag("toggle_guidelines_btn")
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = SunriseGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showGuidelines) "קווי יישור [1]" else "הסתר קווים [1]",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (screenFillLight) Color.Black else TextWhite
                )
            }

            // Fill Light (for dark morning synagogue)
            OutlinedButton(
                onClick = { screenFillLight = !screenFillLight },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (screenFillLight) SunriseRose else DawnBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (screenFillLight) SunriseRose.copy(alpha = 0.2f) else Color.Transparent
                ),
                modifier = Modifier.focusable().testTag("toggle_fill_light_btn")
            ) {
                Icon(Icons.Default.FlashlightOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = SunriseCoral)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (screenFillLight) "תאורת מסך פעילה [2]" else "תאורת מסך [2]",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (screenFillLight) Color.Black else TextWhite
                )
            }

            // Flip Camera
            IconButton(
                onClick = { useFrontCamera = !useFrontCamera },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DawnSurfaceCard)
                    .border(1.dp, DawnBorder, CircleShape)
                    .focusable()
                    .testTag("switch_camera_btn")
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "החלף מצלמה", tint = SunriseGold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Halachic Placement Tip
        Surface(
            color = DawnSurfaceCard,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DawnBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "כללי ההלכה להנחת תפילין של ראש:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SunriseLightGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• הבית חייב לשבת מעל תחילת שורש השערות ולא לגלוש למטה על המצח כלל.\n• מרכז הבית והמעברתא מכוונים בדיוק בין שתי העיניים.\n• הקשר מאחור מונח על העצם התחתונה של הגולגולת (מפרקת).",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = TextMuted
                )
            }
        }
    }
}
