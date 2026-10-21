package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CountdownViewModel
import com.example.ui.screens.CountdownScreen
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
    COUNTDOWN("ספירה לאחור", Icons.Filled.HourglassTop, Icons.Outlined.HourglassTop, "tab_countdown"),
    ZMANIM("זמני היום", Icons.Filled.Schedule, Icons.Outlined.Schedule, "tab_zmanim"),
    GUIDE("מדריך ותיקין", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_guide"),
    SETTINGS("הגדרות", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CountdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetzHaChamaTheme(darkTheme = true) {
                // Ensure natural RTL layout for Hebrew UI
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    var currentTab by remember { mutableStateOf(AppTab.COUNTDOWN) }

                    // Handle back press to navigate back to the main countdown screen
                    BackHandler(enabled = currentTab != AppTab.COUNTDOWN) {
                        currentTab = AppTab.COUNTDOWN
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DawnDeepNight,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            NavigationBar(
                                containerColor = DawnSurface,
                                contentColor = TextWhite,
                                tonalElevation = 8.dp,
                                windowInsets = WindowInsets.navigationBars,
                                modifier = Modifier.testTag("bottom_nav_bar")
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
                                                tint = if (isSelected) DawnDeepNight else TextMuted
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) SunriseLightGold else TextMuted
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = SunriseGold,
                                            selectedIconColor = DawnDeepNight,
                                            selectedTextColor = SunriseLightGold,
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag(tab.tag)
                                    )
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
                                        onTogglePlainNetz = { viewModel.setPlainNetz(it) },
                                        onToggleSound = { viewModel.toggleSound() },
                                        onOpenCitySelector = { currentTab = AppTab.SETTINGS },
                                        onNavigateToGuide = { currentTab = AppTab.GUIDE }
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
                                AppTab.SETTINGS -> {
                                    SettingsScreen(
                                        state = state,
                                        onSelectLocation = { viewModel.selectLocation(it) },
                                        onDetectLocation = { viewModel.detectCurrentLocation(context) },
                                        onSetPlainNetz = { viewModel.setPlainNetz(it) },
                                        onToggleSound = { viewModel.toggleSound() },
                                        onToggleVibration = { viewModel.toggleVibration() },
                                        onToggleTenSecBeep = { viewModel.toggleTenSecBeep() },
                                        onSaveCustomLocation = { name, lat, lon, ele, off ->
                                            viewModel.setCustomLocation(name, lat, lon, ele, off)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
