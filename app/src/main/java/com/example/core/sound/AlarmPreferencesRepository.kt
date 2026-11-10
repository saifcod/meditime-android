package com.example.core.sound

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.alarmDataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_settings_prefs")

enum class AlarmTone(val id: String, val title: String) {
    CHIME("chime", "Gentle Medical Chime"),
    PULSE("pulse", "Rhythmic Heartbeat Pulse"),
    BELL("bell", "Pleasant Morning Bell"),
    DIGITAL("digital", "Clear Digital Alert"),
    SYSTEM_ALARM("system_alarm", "Default System Alarm"),
    CUSTOM_FILE("custom_file", "Custom Audio / Music from Device")
}

data class AlarmPreferences(
    val audibleAlarmEnabled: Boolean = true,
    val selectedTone: AlarmTone = AlarmTone.CHIME,
    val customAudioUri: String? = null,
    val customAudioTitle: String? = null,
    val vibrationEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 10,
    val maxAlarmDurationSeconds: Int = 120, // 2 minutes default timeout
    val spokenReminderEnabled: Boolean = true
)

class AlarmPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val AUDIBLE_ALARM_ENABLED = booleanPreferencesKey("audible_alarm_enabled")
        val SELECTED_TONE_ID = stringPreferencesKey("selected_tone_id")
        val CUSTOM_AUDIO_URI = stringPreferencesKey("custom_audio_uri")
        val CUSTOM_AUDIO_TITLE = stringPreferencesKey("custom_audio_title")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration_minutes")
        val MAX_ALARM_DURATION = intPreferencesKey("max_alarm_duration_seconds")
        val SPOKEN_REMINDER_ENABLED = booleanPreferencesKey("spoken_reminder_enabled")
    }

    val alarmPreferencesFlow: Flow<AlarmPreferences> = context.alarmDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val audibleEnabled = preferences[PreferencesKeys.AUDIBLE_ALARM_ENABLED] ?: true
            val toneId = preferences[PreferencesKeys.SELECTED_TONE_ID] ?: AlarmTone.CHIME.id
            val tone = AlarmTone.values().firstOrNull { it.id == toneId } ?: AlarmTone.CHIME
            val customUri = preferences[PreferencesKeys.CUSTOM_AUDIO_URI]
            val customTitle = preferences[PreferencesKeys.CUSTOM_AUDIO_TITLE]
            val vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
            val snoozeMinutes = preferences[PreferencesKeys.SNOOZE_DURATION] ?: 10
            val maxDuration = preferences[PreferencesKeys.MAX_ALARM_DURATION] ?: 120
            val spokenEnabled = preferences[PreferencesKeys.SPOKEN_REMINDER_ENABLED] ?: true

            AlarmPreferences(
                audibleAlarmEnabled = audibleEnabled,
                selectedTone = tone,
                customAudioUri = customUri,
                customAudioTitle = customTitle,
                vibrationEnabled = vibrationEnabled,
                snoozeDurationMinutes = snoozeMinutes,
                maxAlarmDurationSeconds = maxDuration,
                spokenReminderEnabled = spokenEnabled
            )
        }

    suspend fun setAudibleAlarmEnabled(enabled: Boolean) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIBLE_ALARM_ENABLED] = enabled
        }
    }

    suspend fun setSelectedTone(tone: AlarmTone) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TONE_ID] = tone.id
        }
    }

    suspend fun setCustomAudio(uri: String, title: String) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TONE_ID] = AlarmTone.CUSTOM_FILE.id
            preferences[PreferencesKeys.CUSTOM_AUDIO_URI] = uri
            preferences[PreferencesKeys.CUSTOM_AUDIO_TITLE] = title
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION] = minutes
        }
    }

    suspend fun setMaxAlarmDuration(seconds: Int) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_ALARM_DURATION] = seconds
        }
    }

    suspend fun setSpokenReminderEnabled(enabled: Boolean) {
        context.alarmDataStore.edit { preferences ->
            preferences[PreferencesKeys.SPOKEN_REMINDER_ENABLED] = enabled
        }
    }
}
