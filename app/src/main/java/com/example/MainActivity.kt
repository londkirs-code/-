package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CountdownViewModel
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.JerusalemCompassScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VatikinGuideScreen
import com.example.ui.screens.ZmanimScreen
import com.example.ui.theme.*

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    COUNTDOWN("ספירה", Icons.Filled.HourglassTop, Icons.Outlined.HourglassTop, "tab_countdown"),
    ZMANIM("זמנים", Icons.Filled.Schedule, Icons.Outlined.Schedule, "tab_zmanim"),
    GUIDE("ותיקין", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_guide"),
    COMPASS("מצפן", Icons.Filled.Explore, Icons.Outlined.Explore, "tab_compass"),
    SETTINGS("הגדרות", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CountdownViewModel by viewModels()

    // Active tab state accessible to key events
    private var activeTabState: MutableState<AppTab>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getBooleanExtra("EXTRA_ALARM_RINGING", false)) {
            viewModel.testAlarmNow()
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val ticker by viewModel.tickerState.collectAsStateWithLifecycle()
            val compassState by viewModel.compassState.collectAsStateWithLifecycle()

            NetzHaChamaTheme(themeMode = state.themeMode) {
                val colors = LocalAppColors.current
                val currentDensity = androidx.compose.ui.platform.LocalDensity.current
                val scaledDensity = remember(currentDensity.density, currentDensity.fontScale, state.uiScale) {
                    androidx.compose.ui.unit.Density(
                        density = currentDensity.density * state.uiScale,
                        fontScale = currentDensity.fontScale * state.uiScale
                    )
                }

                // Natural RTL layout for Hebrew UI with Global UI scaling
                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides scaledDensity,
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    val context = LocalContext.current
                    val currentTabState = remember { mutableStateOf(AppTab.COUNTDOWN) }
                    activeTabState = currentTabState
                    var currentTab by currentTabState

                    // Handle back press to return to main countdown screen
                    BackHandler(enabled = currentTab != AppTab.COUNTDOWN) {
                        currentTab = AppTab.COUNTDOWN
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = colors.background,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            Surface(
                                color = colors.surface,
                                tonalElevation = 8.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                NavigationBar(
                                    containerColor = colors.surface,
                                    contentColor = colors.textPrimary,
                                    tonalElevation = 0.dp,
                                    windowInsets = NavigationBarDefaults.windowInsets,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("bottom_nav_bar")
                                ) {
                                    AppTab.values().forEach { tab ->
                                        val isSelected = currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title,
                                                    tint = if (isSelected) colors.gold else colors.textSecondary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) colors.gold else colors.textSecondary,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = colors.gold.copy(alpha = 0.22f),
                                                selectedIconColor = colors.gold,
                                                selectedTextColor = colors.gold,
                                                unselectedIconColor = colors.textSecondary,
                                                unselectedTextColor = colors.textSecondary
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .keypadFocusable(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                                .testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                AppTab.COUNTDOWN -> {
                                    CountdownScreen(
                                        state = state,
                                        ticker = ticker,
                                        onTogglePlainNetz = { viewModel.setPlainNetz(it) },
                                        onToggleSound = { viewModel.toggleSound() },
                                        onToggleTheme = { viewModel.toggleThemeMode() },
                                        onOpenCitySelector = { currentTab = AppTab.SETTINGS },
                                        onNavigateToGuide = { currentTab = AppTab.GUIDE },
                                        onNavigateToCompass = { currentTab = AppTab.COMPASS },
                                        onToggleAlarm = { viewModel.setAlarmEnabled(it) },
                                        onChangeAlarmMinutes = { viewModel.changeAlarmMinutesBeforeNetz(it) },
                                        onSetAlarmMinutes = { viewModel.setAlarmMinutesBeforeNetz(it) },
                                        onTestAlarm = { viewModel.testAlarmNow() },
                                        onSyncSystemClock = { viewModel.syncWithSystemClock(context) }
                                    )
                                }
                                AppTab.ZMANIM -> {
                                    ZmanimScreen(
                                        state = state,
                                        onSelectDate = { viewModel.setSelectedZmanimDate(it) }
                                    )
                                }
                                AppTab.GUIDE -> {
                                    VatikinGuideScreen(
                                        state = state
                                    )
                                }
                                AppTab.COMPASS -> {
                                    JerusalemCompassScreen(
                                        state = state,
                                        compassState = compassState,
                                        onStartListening = { viewModel.compassManager.startListening() },
                                        onStopListening = { viewModel.compassManager.stopListening() },
                                        onRotateManual = { viewModel.rotateCompassManually(it) }
                                    )
                                }
                                AppTab.SETTINGS -> {
                                    SettingsScreen(
                                        state = state,
                                        onSelectLocation = { viewModel.selectLocation(it) },
                                        onDetectLocation = { viewModel.detectCurrentLocation(context) },
                                        onSetPlainNetz = { viewModel.setPlainNetz(it) },
                                        onSetThemeMode = { viewModel.setThemeMode(it) },
                                        onToggleSound = { viewModel.toggleSound() },
                                        onToggleVibration = { viewModel.toggleVibration() },
                                        onToggleTenSecBeep = { viewModel.toggleTenSecBeep() },
                                        onSetUiScale = { viewModel.setUiScale(it) },
                                        onSaveCustomLocation = { name, lat, lon, ele, off ->
                                             viewModel.setCustomLocation(name, lat, lon, ele, off)
                                        },
                                        onToggleAlarm = { viewModel.setAlarmEnabled(it) },
                                        onChangeAlarmMinutes = { viewModel.changeAlarmMinutesBeforeNetz(it) },
                                        onSetAlarmMinutes = { viewModel.setAlarmMinutesBeforeNetz(it) },
                                        onToggleAlarmSound = { viewModel.setAlarmSoundEnabled(it) },
                                        onToggleAlarmVibration = { viewModel.setAlarmVibrationEnabled(it) },
                                        onSetAlarmSnoozeMinutes = { viewModel.setAlarmSnoozeMinutes(it) },
                                        onTestAlarm = { viewModel.testAlarmNow() },
                                        onSyncSystemClock = { viewModel.syncWithSystemClock(context) }
                                    )
                                }
                            }

                            // Dynamic Vatikin Alarm Ringing Full-Screen Modal
                            if (state.alarmIsRinging) {
                                com.example.ui.components.VatikinAlarmRingingDialog(
                                    state = state,
                                    onDismiss = { viewModel.dismissAlarm() },
                                    onSnooze = { viewModel.snoozeAlarm() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("EXTRA_ALARM_RINGING", false)) {
            viewModel.testAlarmNow()
        }
    }

    /**
     * Physical Keypad & Kosher Phone navigation support:
     * - '*' advances forward to next tab
     * - '#' moves backward to previous tab (actions reversed as requested)
     * - '1' to '5' for direct tab access
     * - '7' to toggle Day/Night theme
     * - '8' to toggle Netz type
     * - '9' to cycle UI scaling (Compact, Normal, Large, Extra Large)
     * - '0' to toggle Sound (or dismiss alarm if ringing)
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // If alarm is currently ringing, physical buttons dismiss or snooze it
        if (viewModel.uiState.value.alarmIsRinging) {
            when (keyCode) {
                KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0,
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_SPACE -> {
                    viewModel.dismissAlarm()
                    return true
                }
                KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_POUND -> {
                    viewModel.snoozeAlarm()
                    return true
                }
            }
        }

        val tabState = activeTabState
        if (tabState != null) {
            val tabs = AppTab.values()
            val currentIndex = tabState.value.ordinal

            when (keyCode) {
                KeyEvent.KEYCODE_STAR -> {
                    // Star (*) now advances FORWARD to next tab
                    val nextIndex = (currentIndex + 1) % tabs.size
                    tabState.value = tabs[nextIndex]
                    return true
                }
                KeyEvent.KEYCODE_POUND -> {
                    // Pound (#) now moves BACKWARD to previous tab
                    val prevIndex = (currentIndex - 1 + tabs.size) % tabs.size
                    tabState.value = tabs[prevIndex]
                    return true
                }
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
                    tabState.value = AppTab.COUNTDOWN
                    return true
                }
                KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                    tabState.value = AppTab.ZMANIM
                    return true
                }
                KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
                    tabState.value = AppTab.GUIDE
                    return true
                }
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
                    tabState.value = AppTab.COMPASS
                    return true
                }
                KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
                    tabState.value = AppTab.SETTINGS
                    return true
                }
                KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> {
                    viewModel.toggleThemeMode()
                    return true
                }
                KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> {
                    viewModel.setPlainNetz(!viewModel.uiState.value.isPlainNetz)
                    return true
                }
                KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> {
                    val currentScale = viewModel.uiState.value.uiScale
                    val nextScale = when {
                        currentScale < 0.95f -> 1.0f
                        currentScale < 1.10f -> 1.15f
                        currentScale < 1.25f -> 1.30f
                        else -> 0.85f
                    }
                    viewModel.setUiScale(nextScale)
                    return true
                }
                KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> {
                    viewModel.toggleSound()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
