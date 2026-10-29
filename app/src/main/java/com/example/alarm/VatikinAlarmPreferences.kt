package com.example.alarm

import android.content.Context
import android.content.SharedPreferences

class VatikinAlarmPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vatikin_alarm_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ENABLED, value).apply()

    var minutesBeforeNetz: Int
        get() = prefs.getInt(KEY_MINUTES_BEFORE_NETZ, 45)
        set(value) = prefs.edit().putInt(KEY_MINUTES_BEFORE_NETZ, value).apply()

    var isPlainNetz: Boolean
        get() = prefs.getBoolean(KEY_IS_PLAIN_NETZ, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PLAIN_NETZ, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var snoozeMinutes: Int
        get() = prefs.getInt(KEY_SNOOZE_MINUTES, 5)
        set(value) = prefs.edit().putInt(KEY_SNOOZE_MINUTES, value).apply()

    var nextAlarmEpochMillis: Long
        get() = prefs.getLong(KEY_NEXT_ALARM_EPOCH, 0L)
        set(value) = prefs.edit().putLong(KEY_NEXT_ALARM_EPOCH, value).apply()

    var nextAlarmFormatted: String
        get() = prefs.getString(KEY_NEXT_ALARM_FORMATTED, "--:--") ?: "--:--"
        set(value) = prefs.edit().putString(KEY_NEXT_ALARM_FORMATTED, value).apply()

    companion object {
        private const val KEY_IS_ENABLED = "is_enabled"
        private const val KEY_MINUTES_BEFORE_NETZ = "minutes_before_netz"
        private const val KEY_IS_PLAIN_NETZ = "is_plain_netz"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
        private const val KEY_NEXT_ALARM_EPOCH = "next_alarm_epoch"
        private const val KEY_NEXT_ALARM_FORMATTED = "next_alarm_formatted"
    }
}
