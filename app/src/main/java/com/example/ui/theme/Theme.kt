package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SunriseGold,
    onPrimary = DawnDeepNight,
    primaryContainer = DawnSurfaceVariant,
    onPrimaryContainer = SunriseLightGold,
    secondary = SunriseCoral,
    onSecondary = DawnDeepNight,
    secondaryContainer = DawnSurfaceCard,
    onSecondaryContainer = SunriseLightGold,
    tertiary = SunriseRose,
    background = DawnDeepNight,
    onBackground = TextWhite,
    surface = DawnSurface,
    onSurface = TextWhite,
    surfaceVariant = DawnSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = DawnBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DayPrimary,
    onPrimary = DayOnPrimary,
    primaryContainer = DaySurfaceVariant,
    onPrimaryContainer = DayPrimary,
    secondary = SunriseAmber,
    onSecondary = Color.White,
    tertiary = SunriseRose,
    background = DayBackground,
    onBackground = DayText,
    surface = DaySurface,
    onSurface = DayText,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayTextMuted,
    outline = Color(0xFFE2E8F0)
)

@Composable
fun NetzHaChamaTheme(
    darkTheme: Boolean = true, // Default to deep dawn dark theme for astronomical countdown immersion
    dynamicColor: Boolean = false, // Keep branded golden dawn aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
