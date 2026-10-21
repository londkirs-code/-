package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundVibrationHelper
import com.example.calculator.AstronomicalCalculator
import com.example.calculator.DayZmanim
import com.example.calendar.HebrewDate
import com.example.calendar.HebrewDateHelper
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
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class CountdownUiState(
    val selectedLocation: LocationData = PredefinedLocations.DEFAULT,
    val isPlainNetz: Boolean = false, // false = נץ נראה, true = נץ מישורי
    val currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of(PredefinedLocations.DEFAULT.timeZoneId)),
    val targetNetz: ZonedDateTime = ZonedDateTime.now(),
    val isTomorrowNetz: Boolean = false,
    val remainingMillis: Long = 0L,
    val hours: Long = 0L,
    val minutes: Long = 0L,
    val seconds: Long = 0L,
    val centiseconds: Long = 0L, // tenths/hundredths for smooth precision
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

    private val soundHelper = SoundVibrationHelper(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private var tickerJob: Job? = null
    private var lastTriggeredSecond: Long = -1L
    private var triggeredMilestones = mutableSetOf<String>()

    init {
        recalculateZmanim()
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                updateCountdown()
                delay(40) // ~25 FPS smooth ticker
            }
        }
    }

    private fun updateCountdown() {
        val state = _uiState.value
        val zoneId = ZoneId.of(state.selectedLocation.timeZoneId)
        val now = ZonedDateTime.now(zoneId)

        val todayZ = state.todayZmanim ?: AstronomicalCalculator.calculateZmanim(now.toLocalDate(), state.selectedLocation)
        val tomorrowZ = state.tomorrowZmanim ?: AstronomicalCalculator.calculateZmanim(now.toLocalDate().plusDays(1), state.selectedLocation)

        val todayNetz = if (state.isPlainNetz) todayZ.netzPlain else todayZ.netzVisible
        val tomorrowNetz = if (state.isPlainNetz) tomorrowZ.netzPlain else tomorrowZ.netzVisible

        val (targetNetz, isTomorrow) = if (now.isBefore(todayNetz)) {
            todayNetz to false
        } else {
            tomorrowNetz to true
        }

        val remainingMs = Duration.between(now, targetNetz).toMillis().coerceAtLeast(0L)
        val totalSec = remainingMs / 1000
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        val centis = (remainingMs % 1000) / 10

        val sunAlt = AstronomicalCalculator.getSolarAltitude(now, state.selectedLocation.latitude, state.selectedLocation.longitude)

        // Find active Vatikin step if within 80 minutes before target netz
        val minutesToNetz = (remainingMs / 60000).toInt()
        val activeStep = if (!isTomorrow && minutesToNetz <= 80) {
            VatikinGuideData.STEPS.firstOrNull { minutesToNetz >= it.minutesBeforeNetz } ?: VatikinGuideData.STEPS.last()
        } else null

        // Audio & Haptic alert handling
        handleAlerts(state, remainingMs, totalSec)

        _uiState.update {
            it.copy(
                currentTime = now,
                targetNetz = targetNetz,
                isTomorrowNetz = isTomorrow,
                remainingMillis = remainingMs,
                hours = hrs,
                minutes = mins,
                seconds = secs,
                centiseconds = centis,
                sunAltitudeDegrees = sunAlt,
                activeVatikinStep = activeStep
            )
        }
    }

    private fun handleAlerts(state: CountdownUiState, remainingMs: Long, totalSec: Long) {
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
        val today = LocalDate.now(zoneId)
        val tomorrow = today.plusDays(1)

        val todayZmanim = AstronomicalCalculator.calculateZmanim(today, state.selectedLocation)
        val tomorrowZmanim = AstronomicalCalculator.calculateZmanim(tomorrow, state.selectedLocation)
        val currentHebrew = HebrewDateHelper.fromLocalDate(today)

        val selectedZmanim = AstronomicalCalculator.calculateZmanim(state.selectedZmanimDate, state.selectedLocation)
        val selectedHebrew = HebrewDateHelper.fromLocalDate(state.selectedZmanimDate)

        _uiState.update {
            it.copy(
                todayZmanim = todayZmanim,
                tomorrowZmanim = tomorrowZmanim,
                currentHebrewDate = currentHebrew,
                selectedDayZmanim = selectedZmanim,
                selectedDayHebrewDate = selectedHebrew
            )
        }
    }

    fun selectLocation(location: LocationData) {
        _uiState.update { it.copy(selectedLocation = location, locationError = null) }
        recalculateZmanim()
    }

    fun setPlainNetz(isPlain: Boolean) {
        _uiState.update { it.copy(isPlainNetz = isPlain) }
        recalculateZmanim()
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
    }
}
