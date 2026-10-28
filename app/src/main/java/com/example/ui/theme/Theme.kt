package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AppThemeMode

data class AppThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceCard: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textGold: Color,
    val gold: Color,
    val goldLight: Color,
    val goldGlow: Color,
    val coral: Color,
    val rose: Color,
    val focusBorder: Color
)

val DarkAppColors = AppThemeColors(
    isDark = true,
    background = DawnDeepNight,
    surface = DawnSurface,
    surfaceVariant = DawnSurfaceVariant,
    surfaceCard = DawnSurfaceCard,
    border = DawnBorder,
    textPrimary = TextWhite,
    textSecondary = TextMuted,
    textGold = TextGold,
    gold = SunriseGold,
    goldLight = SunriseLightGold,
    goldGlow = SunriseSunGlow,
    coral = SunriseCoral,
    rose = SunriseRose,
    focusBorder = DawnFocusBorder
)

val LightAppColors = AppThemeColors(
    isDark = false,
    background = DayBackground,
    surface = DaySurface,
    surfaceVariant = DaySurfaceVariant,
    surfaceCard = DaySurfaceCard,
    border = DayBorder,
    textPrimary = DayTextPrimary,
    textSecondary = DayTextSecondary,
    textGold = DayTextGold,
    gold = DayPrimary,
    goldLight = SunriseAmber,
    goldGlow = DayTextGold,
    coral = Color(0xFFC2410C),
    rose = Color(0xFFBE123C),
    focusBorder = DayFocusBorder
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/**
 * Keypad & D-pad focus indicator for physical keys navigation.
 * Draws an animated high-contrast frame/border whenever the element has focus.
 */
@Composable
fun Modifier.keypadFocusable(
    shape: Shape = RoundedCornerShape(14.dp),
    focusBorderColor: Color = LocalAppColors.current.focusBorder,
    defaultBorderColor: Color = Color.Transparent,
    defaultBorderWidth: Dp = 0.dp
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) focusBorderColor else defaultBorderColor,
        label = "focus_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.5.dp else defaultBorderWidth,
        label = "focus_border_width"
    )

    return this
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .border(borderWidth, borderColor, shape)
}

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
    onBackground = DayTextPrimary,
    surface = DaySurface,
    onSurface = DayTextPrimary,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayTextSecondary,
    outline = DayBorder
)

@Composable
fun NetzHaChamaTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val appColors = if (isDark) DarkAppColors else LightAppColors

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
