package com.example.ui.screens

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediTimeApplication
import com.example.core.reminders.ReminderEngine
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class MediTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MediTimeApplication).repository

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val medicines: StateFlow<List<Medicine>> = repository.activeMedicines
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allSchedules: MutableStateFlow<List<MedicationSchedule>> = MutableStateFlow(emptyList())

    val todayDoses: MutableStateFlow<List<DoseRecord>> = MutableStateFlow(emptyList())

    val historyRecords: StateFlow<List<DoseRecord>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        // Safe asynchronous TTS initialization
        try {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    tts = TextToSpeech(application) { status ->
                        if (status == TextToSpeech.SUCCESS) {
                            try {
                                tts?.language = Locale.getDefault()
                                isTtsInitialized = true
                            } catch (e: Exception) {
                                Log.e("MediTimeVM", "TTS language init error", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MediTimeVM", "TTS creation error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MediTimeVM", "Error launching TTS coroutine", e)
        }

        refreshSchedulesAndTodayDoses()
    }

    fun speakVoiceReminder(text: String) {
        try {
            val profile = userProfile.value
            if (profile?.voiceRemindersEnabled == true && isTtsInitialized) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "meditime_tts")
            }
        } catch (e: Exception) {
            Log.e("MediTimeVM", "Error in speakVoiceReminder", e)
        }
    }

    fun refreshSchedulesAndTodayDoses() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val schedules = repository.getAllActiveSchedulesOnce()
                allSchedules.value = schedules

                // Calculate start and end of today
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val endOfDay = cal.timeInMillis

                val records = repository.getDoseRecordsForPeriodOnce(startOfDay, endOfDay).toMutableList()

                // If no doses generated yet today for active schedules, auto-generate SCHEDULED instances
                for (schedule in schedules) {
                    val med = repository.getMedicineById(schedule.medicineId)
                    if (med != null && med.isActive) {
                        val doseCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                            set(Calendar.MINUTE, schedule.timeMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val scheduledTime = doseCal.timeInMillis
                        val exists = records.any { it.medicineId == med.id && it.scheduleId == schedule.id }
                        if (!exists) {
                            val record = DoseRecord(
                                medicineId = med.id,
                                scheduleId = schedule.id,
                                scheduledTimeMillis = scheduledTime,
                                status = if (scheduledTime < System.currentTimeMillis() - 3600_000) "MISSED" else "SCHEDULED",
                                doseInstruction = med.doseInstruction,
                                foodInstruction = med.foodInstruction,
                                medicineNameSnapshot = med.name
                            )
                            val id = repository.insertDoseRecord(record)
                            records.add(record.copy(id = id))
                        }
                    }
                }

                todayDoses.value = records.sortedBy { it.scheduledTimeMillis }
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error in refreshSchedulesAndTodayDoses", e)
            }
        }
    }

    fun saveOnboarding(isSenior: Boolean, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveUserProfile(
                    UserProfile(
                        name = name,
                        seniorMode = isSenior,
                        nightMode = false, // Always default to Light Mode
                        isOnboarded = true
                    )
                )
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error saving onboarding", e)
            }
        }
    }

    fun addMedicineWithSchedules(
        medicine: Medicine,
        times: List<Pair<Int, Int>>,
        selectedDays: List<Int>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val medId = repository.insertMedicine(medicine)
                val savedMed = medicine.copy(id = medId)

                for ((hour, minute) in times) {
                    val schedule = MedicationSchedule(
                        medicineId = medId,
                        timeHour = hour,
                        timeMinute = minute,
                        selectedDaysOfWeek = selectedDays.joinToString(",")
                    )
                    val schedId = repository.insertSchedule(schedule)
                    val savedSched = schedule.copy(id = schedId)

                    // Schedule alarm in Android AlarmManager
                    val nextTrigger = ReminderEngine.calculateNextTriggerMillis(
                        hour = hour,
                        minute = minute,
                        selectedDays = selectedDays
                    )
                    ReminderEngine.scheduleAlarm(getApplication(), savedMed, savedSched, nextTrigger)
                }
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error adding medicine with schedules", e)
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val schedules = repository.getSchedulesForMedicineOnce(medicine.id)
                for (s in schedules) {
                    ReminderEngine.cancelAlarm(getApplication(), s.id)
                }
                repository.deleteMedicine(medicine.id)
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error deleting medicine", e)
            }
        }
    }

    fun toggleSchedule(schedule: MedicationSchedule, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updated = schedule.copy(isEnabled = isEnabled)
                repository.insertSchedule(updated)
                val med = repository.getMedicineById(schedule.medicineId)
                if (med != null) {
                    if (isEnabled) {
                        val days = schedule.selectedDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                        val nextTrigger = ReminderEngine.calculateNextTriggerMillis(schedule.timeHour, schedule.timeMinute, days)
                        ReminderEngine.scheduleAlarm(getApplication(), med, updated, nextTrigger)
                    } else {
                        ReminderEngine.cancelAlarm(getApplication(), schedule.id)
                    }
                }
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling schedule", e)
            }
        }
    }

    fun deleteSchedule(schedule: MedicationSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ReminderEngine.cancelAlarm(getApplication(), schedule.id)
                repository.deleteScheduleById(schedule.id)
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error deleting schedule", e)
            }
        }
    }

    fun markDoseTaken(dose: DoseRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                repository.updateDoseStatus(dose.id, "TAKEN", now)

                // Decrement medicine stock if tracking enabled
                val med = repository.getMedicineById(dose.medicineId)
                if (med != null && med.isStockTrackingEnabled && med.stockQuantity > 0) {
                    repository.updateStock(med.id, med.stockQuantity - 1)
                }

                speakVoiceReminder("Great job taking your ${dose.medicineNameSnapshot}.")
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error marking dose taken", e)
            }
        }
    }

    fun markDoseSkipped(dose: DoseRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                repository.updateDoseStatus(dose.id, "SKIPPED", now)
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error marking dose skipped", e)
            }
        }
    }

    fun snoozeDose(dose: DoseRecord, minutes: Int = 10) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snoozeMillis = System.currentTimeMillis() + minutes * 60 * 1000L
                val med = repository.getMedicineById(dose.medicineId)
                val schedule = allSchedules.value.firstOrNull { it.id == dose.scheduleId }
                if (med != null && schedule != null) {
                    ReminderEngine.scheduleAlarm(getApplication(), med, schedule, snoozeMillis)
                }
                repository.updateDoseStatus(dose.id, "SNOOZED", System.currentTimeMillis())
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error snoozing dose", e)
            }
        }
    }

    fun updateMedicineStock(medicine: Medicine, newStock: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateStock(medicine.id, newStock)
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error updating medicine stock", e)
            }
        }
    }

    fun toggleNightMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = userProfile.value ?: UserProfile()
                repository.saveUserProfile(profile.copy(nightMode = enabled))
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling night mode", e)
            }
        }
    }

    fun toggleSeniorMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = userProfile.value ?: UserProfile()
                repository.saveUserProfile(profile.copy(seniorMode = enabled))
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling senior mode", e)
            }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = userProfile.value ?: UserProfile()
                repository.saveUserProfile(profile.copy(soundEnabled = enabled))
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling sound", e)
            }
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = userProfile.value ?: UserProfile()
                repository.saveUserProfile(profile.copy(vibrationEnabled = enabled))
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling vibration", e)
            }
        }
    }

    fun toggleVoice(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = userProfile.value ?: UserProfile()
                repository.saveUserProfile(profile.copy(voiceRemindersEnabled = enabled))
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error toggling voice", e)
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                for (s in allSchedules.value) {
                    ReminderEngine.cancelAlarm(getApplication(), s.id)
                }
                repository.clearAllData()
                refreshSchedulesAndTodayDoses()
            } catch (e: Exception) {
                Log.e("MediTimeVM", "Error deleting all data", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("MediTimeVM", "Error shutting down TTS", e)
        }
    }
}
