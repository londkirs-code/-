package com.example.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import com.example.MainActivity
import com.example.calculator.AstronomicalCalculator
import com.example.model.LocationData
import com.example.model.PredefinedLocations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object VatikinAlarmManager {
    private const val TAG = "VatikinAlarmManager"

    const val ACTION_VATIKIN_ALARM = "com.example.ACTION_VATIKIN_ALARM"
    const val ACTION_DISMISS_ALARM = "com.example.ACTION_DISMISS_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.example.ACTION_SNOOZE_ALARM"

    const val EXTRA_MINUTES_BEFORE_NETZ = "extra_minutes_before_netz"
    const val EXTRA_LOCATION_NAME = "extra_location_name"
    const val EXTRA_NETZ_TIME_FORMATTED = "extra_netz_time_formatted"

    private val _isRingingState = MutableStateFlow(false)
    val isRingingState: StateFlow<Boolean> = _isRingingState.asStateFlow()

    fun setRinging(ringing: Boolean) {
        _isRingingState.value = ringing
    }

    /**
     * Calculates the exact upcoming trigger time for the Vatikin alarm:
     * - Checks today's Netz minus [minutesBeforeNetz].
     * - If that instant is still in the future, that is today's wake-up time.
     * - Otherwise, calculates tomorrow's Netz minus [minutesBeforeNetz].
     */
    fun calculateNextAlarmDateTime(
        location: LocationData,
        minutesBeforeNetz: Int,
        isPlainNetz: Boolean
    ): ZonedDateTime {
        val zoneId = try {
            ZoneId.of(location.timeZoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val tomorrow = today.plusDays(1)

        val todayZmanim = AstronomicalCalculator.calculateZmanim(today, location)
        val todayNetz = if (isPlainNetz) todayZmanim.netzPlain else todayZmanim.netzVisible
        val todayWakeTime = todayNetz.minusMinutes(minutesBeforeNetz.toLong())

        return if (now.isBefore(todayWakeTime)) {
            todayWakeTime
        } else {
            val tomorrowZmanim = AstronomicalCalculator.calculateZmanim(tomorrow, location)
            val tomorrowNetz = if (isPlainNetz) tomorrowZmanim.netzPlain else tomorrowZmanim.netzVisible
            tomorrowNetz.minusMinutes(minutesBeforeNetz.toLong())
        }
    }

    /**
     * Schedules the dynamic Vatikin alarm via Android AlarmManager.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleVatikinAlarm(
        context: Context,
        location: LocationData = PredefinedLocations.DEFAULT,
        minutesBeforeNetz: Int = 45,
        isPlainNetz: Boolean = false
    ): Long {
        val prefs = VatikinAlarmPreferences(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val nextWakeTime = calculateNextAlarmDateTime(location, minutesBeforeNetz, isPlainNetz)
        val triggerEpochMillis = nextWakeTime.toInstant().toEpochMilli()

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = nextWakeTime.format(timeFormatter)

        prefs.isEnabled = true
        prefs.minutesBeforeNetz = minutesBeforeNetz
        prefs.isPlainNetz = isPlainNetz
        prefs.nextAlarmEpochMillis = triggerEpochMillis
        prefs.nextAlarmFormatted = formattedTime

        val intent = Intent(context, VatikinAlarmReceiver::class.java).apply {
            action = ACTION_VATIKIN_ALARM
            putExtra(EXTRA_MINUTES_BEFORE_NETZ, minutesBeforeNetz)
            putExtra(EXTRA_LOCATION_NAME, location.nameHebrew)
            val netzTime = nextWakeTime.plusMinutes(minutesBeforeNetz.toLong())
            putExtra(EXTRA_NETZ_TIME_FORMATTED, netzTime.format(timeFormatter))
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags)

        val showIntent = Intent(context, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingShowIntent = PendingIntent.getActivity(context, 1002, showIntent, flags)

        try {
            // Using setAlarmClock ensures optimal priority and shows the system alarm indicator
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerEpochMillis, pendingShowIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Vatikin alarm scheduled for $formattedTime ($triggerEpochMillis)")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException on setAlarmClock, falling back to setExactAndAllowWhileIdle: ${e.message}")
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule alarm: ${e2.message}", e2)
            }
        }

        return triggerEpochMillis
    }

    /**
     * Cancels the currently scheduled Vatikin alarm.
     */
    fun cancelVatikinAlarm(context: Context) {
        val prefs = VatikinAlarmPreferences(context)
        prefs.isEnabled = false
        prefs.nextAlarmEpochMillis = 0L
        prefs.nextAlarmFormatted = "--:--"

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, VatikinAlarmReceiver::class.java).apply {
            action = ACTION_VATIKIN_ALARM
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags)

        alarmManager.cancel(pendingIntent)
        VatikinAlarmRinger.stopRinging()
        setRinging(false)
        Log.d(TAG, "Vatikin alarm cancelled")
    }

    /**
     * Dismisses the currently ringing alarm and automatically schedules for next day's Netz!
     */
    fun dismissAlarm(context: Context, location: LocationData = PredefinedLocations.DEFAULT) {
        VatikinAlarmRinger.stopRinging()
        setRinging(false)
        VatikinAlarmReceiver.cancelNotification(context)

        val prefs = VatikinAlarmPreferences(context)
        if (prefs.isEnabled) {
            // Reschedule for next upcoming Netz
            scheduleVatikinAlarm(context, location, prefs.minutesBeforeNetz, prefs.isPlainNetz)
        }
    }

    /**
     * Snoozes the alarm for the configured snooze duration (default 5 minutes).
     */
    fun snoozeAlarm(context: Context, snoozeMinutes: Int = 5) {
        VatikinAlarmRinger.stopRinging()
        setRinging(false)
        VatikinAlarmReceiver.cancelNotification(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTimeMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, VatikinAlarmReceiver::class.java).apply {
            action = ACTION_VATIKIN_ALARM
            putExtra(EXTRA_MINUTES_BEFORE_NETZ, 0)
            putExtra(EXTRA_LOCATION_NAME, "נודניק")
            putExtra(EXTRA_NETZ_TIME_FORMATTED, "נודניק")
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 1003, intent, flags)

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent)
            Log.d(TAG, "Vatikin alarm snoozed for $snoozeMinutes minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Error snoozing alarm: ${e.message}", e)
        }
    }

    /**
     * Allows syncing/creating an alarm in the Android device's default System Clock app.
     */
    fun syncWithSystemClock(
        context: Context,
        location: LocationData,
        minutesBeforeNetz: Int,
        isPlainNetz: Boolean
    ): Boolean {
        return try {
            val nextAlarm = calculateNextAlarmDateTime(location, minutesBeforeNetz, isPlainNetz)
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, nextAlarm.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, nextAlarm.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "תפילת ותיקין (${location.nameHebrew})")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set system alarm: ${e.message}", e)
            false
        }
    }
}
