package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundVibrationHelper
import com.example.calculator.AstronomicalCalculator
import com.example.calculator.DayZmanim
import com.example.calendar.HebrewDate
import com.example.calendar.HebrewDateHelper
import com.example.compass.CompassSensorManager
import com.example.compass.CompassState
import com.example.model.AppThemeMode
import com.example.model.LocationData
import com.example.model.PredefinedLocations
import com.example.model.VatikinGuideData
import com.example.model.VatikinStep
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Immutable
data class TickerState(
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val centiseconds: Long = 0,
    val remainingMillis: Long = 0,
    val isUrgent: Boolean = false,
    val isFinalTenSeconds: Boolean = false
)

@Immutable
data class CountdownUiState(
    val selectedLocation: LocationData = PredefinedLocations.DEFAULT,
    val isPlainNetz: Boolean = false, // false = נץ נראה, true = נץ מישורי
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val targetNetz: ZonedDateTime? = null,
    val targetNetzTimeFormatted: String = "--:--:--",
    val isTomorrowNetz: Boolean = false,
    val sunAltitudeDegrees: Double = 0.0,
    val todayZmanim: DayZmanim? = null,
    val tomorrowZmanim: DayZmanim? = null,
    val currentHebrewDate: HebrewDate? = null,
    val activeVatikinStep: VatikinStep? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val tenSecBeepEnabled: Boolean = true,
    val isLocating: Boolean = false,
    val locationError: String? = null,
    val selectedZmanimDate: LocalDate = LocalDate.now(),
    val selectedDayZmanim: DayZmanim? = null,
    val selectedDayHebrewDate: HebrewDate? = null
)

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    private val _tickerState = MutableStateFlow(TickerState())
    val tickerState: StateFlow<TickerState> = _tickerState.asStateFlow()

    val compassManager = CompassSensorManager(application)
    val compassState: StateFlow<CompassState> = compassManager.compassState

    private val soundHelper = SoundVibrationHelper(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private var tickerJob: Job? = null
    private var lastTriggeredSecond: Long = -1L
    private var triggeredMilestones = mutableSetOf<String>()

    // Cached target netz epoch millis for ultra-fast, allocation-free countdown ticks
    @Volatile
    private var targetNetzEpochMs: Long = 0L

    @Volatile
    private var lastSunAltitudeUpdateMs: Long = 0L

    init {
        recalculateZmanim()
        compassManager.updateCoordinates(_uiState.value.selectedLocation.latitude, _uiState.value.selectedLocation.longitude)
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val nowMs = System.currentTimeMillis()
                val remainingMs = (targetNetzEpochMs - nowMs).coerceAtLeast(0L)
                val totalSec = remainingMs / 1000
                val hrs = totalSec / 3600
                val mins = (totalSec % 3600) / 60
                val secs = totalSec % 60
                val centis = (remainingMs % 1000) / 10

                val isUrgent = remainingMs in 1..59_999
                val isFinalTen = remainingMs in 1..10_999

                _tickerState.value = TickerState(
                    hours = hrs,
                    minutes = mins,
                    seconds = secs,
                    centiseconds = centis,
                    remainingMillis = remainingMs,
                    isUrgent = isUrgent,
                    isFinalTenSeconds = isFinalTen
                )

                // Handle audio & haptics efficiently
                handleAlerts(remainingMs, totalSec)

                // Update low-frequency items (Sun altitude & active prayer step) every 10 seconds or when remaining hits 0
                if (nowMs - lastSunAltitudeUpdateMs > 10_000 || remainingMs == 0L) {
                    lastSunAltitudeUpdateMs = nowMs
                    updateLowFrequencyState(remainingMs)
                }

                // Adaptive delay: 100ms when under 1 minute for smooth tenth-seconds; 500ms when far away to save 80% battery/CPU
                val delayTime = if (isUrgent) 100L else 500L
                delay(delayTime)
            }
        }
    }

    private fun updateLowFrequencyState(remainingMs: Long) {
        val state = _uiState.value
        val zoneId = ZoneId.of(state.selectedLocation.timeZoneId)
        val now = ZonedDateTime.now(zoneId)

        // Sun altitude (computed only once every 10-15s instead of 25x/sec)
        val sunAlt = AstronomicalCalculator.getSolarAltitude(
            now,
            state.selectedLocation.latitude,
            state.selectedLocation.longitude
        )

        val minutesToNetz = (remainingMs / 60000).toInt()
        val activeStep = if (!state.isTomorrowNetz && minutesToNetz in 0..80) {
            VatikinGuideData.STEPS.firstOrNull { minutesToNetz >= it.minutesBeforeNetz } ?: VatikinGuideData.STEPS.last()
        } else null

        // Check if today's netz just passed, re-anchor target if needed
        val todayNetz = if (state.isPlainNetz) state.todayZmanim?.netzPlain else state.todayZmanim?.netzVisible
        if (todayNetz != null && now.isAfter(todayNetz) && !state.isTomorrowNetz) {
            recalculateZmanim()
            return
        }

        _uiState.update {
            it.copy(
                sunAltitudeDegrees = sunAlt,
                activeVatikinStep = activeStep
            )
        }
    }

    private fun handleAlerts(remainingMs: Long, totalSec: Long) {
        val state = _uiState.value
        if (!state.soundEnabled && !state.vibrationEnabled) return

        // 1. Ten second countdown beeps
        if (state.tenSecBeepEnabled && remainingMs in 1..10_999) {
            if (totalSec != lastTriggeredSecond && totalSec in 1..10) {
                lastTriggeredSecond = totalSec
                soundHelper.playCountdownBeep()
            }
        }

        // 2. Exact Netz arrival alert (0 seconds remaining)
        if (remainingMs in 0..600 && lastTriggeredSecond != 0L) {
            lastTriggeredSecond = 0L
            soundHelper.playNetzArrivalAlert()
        }

        // 3. Milestone warnings (15m, 5m, 1m)
        val mins = totalSec / 60
        val secInMin = totalSec % 60
        if (secInMin == 0L && totalSec > 10) {
            val key = "${mins}m"
            if ((mins == 15L || mins == 5L || mins == 1L) && !triggeredMilestones.contains(key)) {
                triggeredMilestones.add(key)
                soundHelper.playMilestoneWarning()
            }
        }
    }

    fun recalculateZmanim() {
        val state = _uiState.value
        val zoneId = ZoneId.of(state.selectedLocation.timeZoneId)
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val tomorrow = today.plusDays(1)

        val todayZmanim = AstronomicalCalculator.calculateZmanim(today, state.selectedLocation)
        val tomorrowZmanim = AstronomicalCalculator.calculateZmanim(tomorrow, state.selectedLocation)

        val todayNetz = if (state.isPlainNetz) todayZmanim.netzPlain else todayZmanim.netzVisible
        val tomorrowNetz = if (state.isPlainNetz) tomorrowZmanim.netzPlain else tomorrowZmanim.netzVisible

        val (targetNetz, isTomorrow) = if (now.isBefore(todayNetz)) {
            todayNetz to false
        } else {
            tomorrowNetz to true
        }

        targetNetzEpochMs = targetNetz.toInstant().toEpochMilli()

        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        val currentHebrew = HebrewDateHelper.fromLocalDate(today)
        val selectedZmanim = AstronomicalCalculator.calculateZmanim(state.selectedZmanimDate, state.selectedLocation)
        val selectedHebrew = HebrewDateHelper.fromLocalDate(state.selectedZmanimDate)

        val sunAlt = AstronomicalCalculator.getSolarAltitude(now, state.selectedLocation.latitude, state.selectedLocation.longitude)

        _uiState.update {
            it.copy(
                todayZmanim = todayZmanim,
                tomorrowZmanim = tomorrowZmanim,
                targetNetz = targetNetz,
                targetNetzTimeFormatted = targetNetz.format(timeFormatter),
                isTomorrowNetz = isTomorrow,
                currentHebrewDate = currentHebrew,
                selectedDayZmanim = selectedZmanim,
                selectedDayHebrewDate = selectedHebrew,
                sunAltitudeDegrees = sunAlt
            )
        }
    }

    fun selectLocation(location: LocationData) {
        _uiState.update { it.copy(selectedLocation = location, locationError = null) }
        compassManager.updateCoordinates(location.latitude, location.longitude)
        recalculateZmanim()
    }

    fun setPlainNetz(isPlain: Boolean) {
        _uiState.update { it.copy(isPlainNetz = isPlain) }
        recalculateZmanim()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleThemeMode() {
        val next = when (_uiState.value.themeMode) {
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
        }
        setThemeMode(next)
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun toggleTenSecBeep() {
        _uiState.update { it.copy(tenSecBeepEnabled = !it.tenSecBeepEnabled) }
    }

    fun setSelectedZmanimDate(date: LocalDate) {
        val selectedZmanim = AstronomicalCalculator.calculateZmanim(date, _uiState.value.selectedLocation)
        val selectedHebrew = HebrewDateHelper.fromLocalDate(date)
        _uiState.update {
            it.copy(
                selectedZmanimDate = date,
                selectedDayZmanim = selectedZmanim,
                selectedDayHebrewDate = selectedHebrew
            )
        }
    }

    fun setCustomLocation(name: String, lat: Double, lon: Double, elevation: Double, offsetMinutes: Int) {
        val customLoc = LocationData(
            id = "custom",
            nameHebrew = name.ifBlank { "מיקום מותאם אישית" },
            nameEnglish = "Custom Location",
            latitude = lat,
            longitude = lon,
            elevationMeters = elevation,
            timeZoneId = ZoneId.systemDefault().id,
            visibleNetzOffsetMinutes = offsetMinutes
        )
        selectLocation(customLoc)
    }

    @SuppressLint("MissingPermission")
    fun detectCurrentLocation(context: Context) {
        _uiState.update { it.copy(isLocating = true, locationError = null) }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        val gpsLoc = LocationData(
                            id = "gps_current",
                            nameHebrew = "מיקום נוכחי (GPS)",
                            nameEnglish = "Current GPS Location",
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            elevationMeters = loc.altitude.coerceAtLeast(0.0),
                            timeZoneId = ZoneId.systemDefault().id,
                            visibleNetzOffsetMinutes = 0
                        )
                        selectLocation(gpsLoc)
                        _uiState.update { it.copy(isLocating = false) }
                    } else {
                        _uiState.update { it.copy(isLocating = false, locationError = "לא ניתן לקבל מיקום כרגע") }
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(isLocating = false, locationError = e.localizedMessage ?: "שגיאת מיקום") }
                }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLocating = false, locationError = e.localizedMessage) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        soundHelper.release()
        compassManager.stopListening()
    }
}
