package com.example.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class VatikinAlarmReceiver : BroadcastReceiver() {

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "VatikinAlarmReceiver onReceive action=$action")

        val prefs = VatikinAlarmPreferences(context)

        when (action) {
            VatikinAlarmManager.ACTION_DISMISS_ALARM -> {
                VatikinAlarmManager.dismissAlarm(context)
            }
            VatikinAlarmManager.ACTION_SNOOZE_ALARM -> {
                VatikinAlarmManager.snoozeAlarm(context, prefs.snoozeMinutes)
            }
            VatikinAlarmManager.ACTION_VATIKIN_ALARM, null -> {
                // Acquire wakelock to awaken CPU/screen for alarm
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "NetzHaChama:VatikinAlarmWakeLock"
                )
                wakeLock?.acquire(60_000L) // 1 minute timeout

                // Start ringing audio and vibration
                VatikinAlarmRinger.startRinging(
                    context = context,
                    playSound = prefs.soundEnabled,
                    vibrate = prefs.vibrationEnabled
                )
                VatikinAlarmManager.setRinging(true)

                val minutesBefore = intent.getIntExtra(VatikinAlarmManager.EXTRA_MINUTES_BEFORE_NETZ, prefs.minutesBeforeNetz)
                val locationName = intent.getStringExtra(VatikinAlarmManager.EXTRA_LOCATION_NAME) ?: "תפילת ותיקין"
                val netzTime = intent.getStringExtra(VatikinAlarmManager.EXTRA_NETZ_TIME_FORMATTED) ?: ""

                showAlarmNotification(context, minutesBefore, locationName, netzTime)
            }
        }
    }

    private fun showAlarmNotification(
        context: Context,
        minutesBefore: Int,
        locationName: String,
        netzTime: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high-priority alarm notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "שעון מעורר ותיקין",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "התרעות שעון מעורר לקימה לתפילת ותיקין לפי נץ החמה"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(
                    alarmUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        // Full Screen Intent to open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ALARM_RINGING", true)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 2001, openAppIntent, flags)

        // Dismiss Action
        val dismissIntent = Intent(context, VatikinAlarmReceiver::class.java).apply {
            action = VatikinAlarmManager.ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(context, 2002, dismissIntent, flags)

        // Snooze Action
        val snoozeIntent = Intent(context, VatikinAlarmReceiver::class.java).apply {
            action = VatikinAlarmManager.ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 2003, snoozeIntent, flags)

        val title = "הגיע זמן קימה לתפילת ותיקין!"
        val contentText = if (minutesBefore > 0) {
            "בעוד $minutesBefore דקות נץ החמה ב-$netzTime ($locationName)"
        } else {
            "תזכורת נודניק - קום לתפילת ותיקין!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.mipmap.ic_launcher, "כיבוי שעון", dismissPendingIntent)
            .addAction(R.mipmap.ic_launcher, "נודניק 5 דק'", snoozePendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "VatikinAlarmReceiver"
        const val CHANNEL_ID = "vatikin_alarm_channel"
        const val NOTIFICATION_ID = 7701

        fun cancelNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}
