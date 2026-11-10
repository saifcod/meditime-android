package com.example.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

object AlarmSoundManager {
    private const val TAG = "AlarmSoundManager"

    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var isPlaying = false
    private var autoTimeoutJob: Job? = null
    private var vibrationJob: Job? = null
    private var tonePulseJob: Job? = null

    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Start playing the medication alarm with sound, vibration, and auto-timeout.
     * Supports custom user audio picked from device gallery/storage.
     */
    fun startAlarm(
        context: Context,
        medicineName: String = "",
        timeoutSeconds: Int = 120,
        specificTone: AlarmTone? = null,
        enableVibration: Boolean = true
    ) {
        synchronized(this) {
            if (isPlaying) {
                Log.d(TAG, "Alarm sound already playing, ignoring duplicate request")
                return
            }
            isPlaying = true
        }

        managerScope.launch {
            try {
                val repo = AlarmPreferencesRepository(context)
                val prefs = repo.alarmPreferencesFlow.first()

                if (!prefs.audibleAlarmEnabled && !enableVibration) {
                    Log.d(TAG, "Audible alarm and vibration are both disabled in settings")
                    return@launch
                }

                val toneToPlay = specificTone ?: prefs.selectedTone
                val shouldVibrate = enableVibration && prefs.vibrationEnabled
                val timeout = if (timeoutSeconds > 0) timeoutSeconds else prefs.maxAlarmDurationSeconds

                if (prefs.audibleAlarmEnabled) {
                    playTone(context, toneToPlay, prefs.customAudioUri)
                }

                if (shouldVibrate) {
                    startVibrationPattern(context)
                }

                // Schedule auto timeout
                autoTimeoutJob?.cancel()
                autoTimeoutJob = managerScope.launch {
                    delay(timeout * 1000L)
                    Log.d(TAG, "Alarm reached maximum duration ($timeout s). Auto-stopping.")
                    stopAlarm(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting alarm sound", e)
            }
        }
    }

    private fun playTone(context: Context, tone: AlarmTone, customUriString: String? = null) {
        stopAudioPlayback()

        when (tone) {
            AlarmTone.CUSTOM_FILE -> {
                if (!customUriString.isNullOrBlank()) {
                    playCustomAudioUri(context, customUriString)
                } else {
                    playSystemAlarmRingtone(context)
                }
            }
            AlarmTone.SYSTEM_ALARM -> {
                playSystemAlarmRingtone(context)
            }
            AlarmTone.CHIME -> {
                playSynthesizedTonePattern(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 800, 1200)
            }
            AlarmTone.PULSE -> {
                playSynthesizedTonePattern(ToneGenerator.TONE_PROP_BEEP2, 400, 600)
            }
            AlarmTone.BELL -> {
                playSynthesizedTonePattern(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600, 1000)
            }
            AlarmTone.DIGITAL -> {
                playSynthesizedTonePattern(ToneGenerator.TONE_SUP_RINGTONE, 900, 1100)
            }
        }
    }

    private fun playCustomAudioUri(context: Context, uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing custom audio URI $uriString, falling back to system alarm", e)
            playSystemAlarmRingtone(context)
        }
    }

    private fun playSystemAlarmRingtone(context: Context) {
        try {
            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing system alarm ringtone, falling back to tone generator", e)
            playSynthesizedTonePattern(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 800, 1200)
        }
    }

    private fun playSynthesizedTonePattern(toneType: Int, durationMs: Int, pauseMs: Int) {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 85)
            tonePulseJob?.cancel()
            tonePulseJob = managerScope.launch(Dispatchers.Default) {
                while (isActive && isPlaying) {
                    toneGenerator?.startTone(toneType, durationMs)
                    delay((durationMs + pauseMs).toLong())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ToneGenerator error", e)
        }
    }

    /**
     * Preview a specific tone or custom file inside settings without locking up the full alarm cycle.
     */
    fun previewTone(context: Context, tone: AlarmTone, customUriString: String? = null, onFinished: () -> Unit = {}) {
        stopAlarm(context)
        managerScope.launch {
            try {
                if (tone == AlarmTone.CUSTOM_FILE && !customUriString.isNullOrBlank()) {
                    val uri = Uri.parse(customUriString)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = false
                        prepare()
                        start()
                    }
                    delay(4000)
                    stopAudioPlayback()
                } else if (tone == AlarmTone.SYSTEM_ALARM) {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        isLooping = false
                        prepare()
                        start()
                    }
                    delay(3000)
                    stopAudioPlayback()
                } else {
                    val gen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                    val toneType = when (tone) {
                        AlarmTone.CHIME -> ToneGenerator.TONE_CDMA_HIGH_PBX_L
                        AlarmTone.PULSE -> ToneGenerator.TONE_PROP_BEEP2
                        AlarmTone.BELL -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                        AlarmTone.DIGITAL -> ToneGenerator.TONE_SUP_RINGTONE
                        else -> ToneGenerator.TONE_CDMA_HIGH_PBX_L
                    }
                    gen.startTone(toneType, 1500)
                    delay(1600)
                    gen.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Preview error", e)
            } finally {
                onFinished()
            }
        }
    }

    private fun startVibrationPattern(context: Context) {
        vibrationJob?.cancel()
        vibrationJob = managerScope.launch(Dispatchers.Default) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                } ?: return@launch

                if (!vibrator.hasVibrator()) return@launch

                val timings = longArrayOf(0, 500, 200, 500, 1000)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Vibration error", e)
            }
        }
    }

    private fun stopVibration(context: Context) {
        vibrationJob?.cancel()
        vibrationJob = null
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator", e)
        }
    }

    private fun stopAudioPlayback() {
        tonePulseJob?.cancel()
        tonePulseJob = null

        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing toneGenerator", e)
        }

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing mediaPlayer", e)
        }
    }

    /**
     * Stop the alarm sound and vibration immediately and release resources.
     */
    fun stopAlarm(context: Context) {
        synchronized(this) {
            isPlaying = false
        }
        autoTimeoutJob?.cancel()
        autoTimeoutJob = null
        stopAudioPlayback()
        stopVibration(context)
        Log.d(TAG, "Medication alarm sound and vibration stopped")
    }

    fun isAlarmActive(): Boolean = isPlaying
}
