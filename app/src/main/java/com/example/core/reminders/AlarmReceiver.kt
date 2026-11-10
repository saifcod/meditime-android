package com.example.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.notifications.NotificationHelper
import com.example.data.local.MediTimeDatabase
import com.example.data.local.entities.DoseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderEngine.ACTION_TRIGGER_REMINDER) return

        val medicineId = intent.getLongExtra(ReminderEngine.EXTRA_MEDICINE_ID, -1L)
        val scheduleId = intent.getLongExtra(ReminderEngine.EXTRA_SCHEDULE_ID, -1L)
        val scheduledTimeMillis = intent.getLongExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, System.currentTimeMillis())
        val medicineName = intent.getStringExtra(ReminderEngine.EXTRA_MEDICINE_NAME) ?: "Medicine"
        val doseInstruction = intent.getStringExtra(ReminderEngine.EXTRA_DOSE_INSTRUCTION) ?: "1 Dose"
        val foodInstruction = intent.getStringExtra(ReminderEngine.EXTRA_FOOD_INSTRUCTION) ?: ""

        if (medicineId == -1L || scheduleId == -1L) return

        Log.d("AlarmReceiver", "Triggered alarm for medicine: $medicineName, scheduleId: $scheduleId")

        // 1. Show notification immediately
        NotificationHelper.showDoseReminderNotification(
            context = context,
            medicineId = medicineId,
            scheduleId = scheduleId,
            scheduledTimeMillis = scheduledTimeMillis,
            medicineName = medicineName,
            doseInstruction = doseInstruction,
            foodInstruction = foodInstruction
        )

        // 2. Validate against database and record a SCHEDULED dose if not present, and reschedule next recurrence
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MediTimeDatabase.getDatabase(context)
                val medicine = db.dao().getMedicineById(medicineId)
                if (medicine != null && medicine.isActive) {
                    val existing = db.dao().findDoseRecord(medicineId, scheduleId, scheduledTimeMillis)
                    if (existing == null) {
                        db.dao().insertDoseRecord(
                            DoseRecord(
                                medicineId = medicineId,
                                scheduleId = scheduleId,
                                scheduledTimeMillis = scheduledTimeMillis,
                                status = "SCHEDULED",
                                doseInstruction = doseInstruction,
                                foodInstruction = foodInstruction,
                                medicineNameSnapshot = medicine.name
                            )
                        )
                    }

                    // Reschedule next recurrence for this schedule
                    val schedules = db.dao().getSchedulesForMedicineOnce(medicineId)
                    val currentSchedule = schedules.find { it.id == scheduleId }
                    if (currentSchedule != null && currentSchedule.isEnabled) {
                        val days = currentSchedule.selectedDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                        val nextMillis = ReminderEngine.calculateNextTriggerMillis(
                            hour = currentSchedule.timeHour,
                            minute = currentSchedule.timeMinute,
                            selectedDays = days,
                            fromTimeMillis = System.currentTimeMillis() + 60_000 // Look ahead from 1 min later
                        )
                        ReminderEngine.scheduleAlarm(context, medicine, currentSchedule, nextMillis)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm record", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
