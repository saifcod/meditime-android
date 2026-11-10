package com.example.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.reminders.ReminderEngine
import com.example.data.local.MediTimeDatabase
import com.example.data.local.entities.DoseRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val medicineId = intent.getLongExtra(ReminderEngine.EXTRA_MEDICINE_ID, -1L)
        val scheduleId = intent.getLongExtra(ReminderEngine.EXTRA_SCHEDULE_ID, -1L)
        val scheduledTimeMillis = intent.getLongExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, System.currentTimeMillis())
        val medicineName = intent.getStringExtra(ReminderEngine.EXTRA_MEDICINE_NAME) ?: "Medicine"

        // Dismiss notification
        NotificationHelper.dismissNotification(context, scheduleId.toInt())

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MediTimeDatabase.getDatabase(context)
                val existing = db.dao().findDoseRecord(medicineId, scheduleId, scheduledTimeMillis)

                when (action) {
                    NotificationHelper.ACTION_DOSE_TAKEN -> {
                        val now = System.currentTimeMillis()
                        if (existing != null) {
                            db.dao().updateDoseStatus(existing.id, "TAKEN", now)
                        } else {
                            db.dao().insertDoseRecord(
                                DoseRecord(
                                    medicineId = medicineId,
                                    scheduleId = scheduleId,
                                    scheduledTimeMillis = scheduledTimeMillis,
                                    actionTimeMillis = now,
                                    status = "TAKEN",
                                    medicineNameSnapshot = medicineName
                                )
                            )
                        }

                        // Decrement stock if tracking enabled
                        val med = db.dao().getMedicineById(medicineId)
                        if (med != null && med.isStockTrackingEnabled && med.stockQuantity > 0) {
                            db.dao().updateMedicineStock(med.id, med.stockQuantity - 1)
                        }
                        Log.d("NotificationAction", "Marked dose as TAKEN for $medicineName")
                    }

                    NotificationHelper.ACTION_DOSE_SKIP -> {
                        val now = System.currentTimeMillis()
                        if (existing != null) {
                            db.dao().updateDoseStatus(existing.id, "SKIPPED", now)
                        } else {
                            db.dao().insertDoseRecord(
                                DoseRecord(
                                    medicineId = medicineId,
                                    scheduleId = scheduleId,
                                    scheduledTimeMillis = scheduledTimeMillis,
                                    actionTimeMillis = now,
                                    status = "SKIPPED",
                                    medicineNameSnapshot = medicineName
                                )
                            )
                        }
                        Log.d("NotificationAction", "Marked dose as SKIPPED for $medicineName")
                    }

                    NotificationHelper.ACTION_DOSE_SNOOZE -> {
                        val snoozeMillis = System.currentTimeMillis() + 10 * 60 * 1000L // 10 minutes
                        val med = db.dao().getMedicineById(medicineId)
                        if (med != null) {
                            val dummySchedule = db.dao().getSchedulesForMedicineOnce(medicineId).firstOrNull { it.id == scheduleId }
                            if (dummySchedule != null) {
                                ReminderEngine.scheduleAlarm(context, med, dummySchedule, snoozeMillis)
                            }
                        }
                        Log.d("NotificationAction", "Snoozed dose 10m for $medicineName")
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationAction", "Error updating dose from notification action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
