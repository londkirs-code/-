package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.PredefinedLocations

class VatikinBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("VatikinBootReceiver", "Received action: $action")

        val prefs = VatikinAlarmPreferences(context)
        if (prefs.isEnabled) {
            Log.d("VatikinBootReceiver", "Rescheduling active Vatikin alarm after boot/time change")
            VatikinAlarmManager.scheduleVatikinAlarm(
                context = context,
                location = PredefinedLocations.DEFAULT,
                minutesBeforeNetz = prefs.minutesBeforeNetz,
                isPlainNetz = prefs.isPlainNetz
            )
        }
    }
}
