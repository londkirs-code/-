package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundVibrationHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
    } catch (e: Exception) {
        Log.e("SoundVibrationHelper", "Failed to init ToneGenerator", e)
        null
    }

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Short beep for countdown seconds (e.g. 10..9..8..1)
     */
    fun playCountdownBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            vibrate(60)
        } catch (e: Exception) {
            Log.e("SoundVibrationHelper", "Error playing beep", e)
        }
    }

    /**
     * Netz Arrival Chime / Triumph melody (High distinctive chord at exact sunrise)
     */
    fun playNetzArrivalAlert() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 700)
            vibratePattern(longArrayOf(0, 200, 100, 200, 100, 400))
        } catch (e: Exception) {
            Log.e("SoundVibrationHelper", "Error playing netz alert", e)
        }
    }

    /**
     * Milestone warning (e.g. 15m, 5m, 1m)
     */
    fun playMilestoneWarning() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            vibratePattern(longArrayOf(0, 150, 100, 150))
        } catch (e: Exception) {
            Log.e("SoundVibrationHelper", "Error playing milestone alert", e)
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundVibrationHelper", "Vibration failed", e)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundVibrationHelper", "Vibration pattern failed", e)
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
