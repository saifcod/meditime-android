package com.example.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.MediTimeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("BootReceiver", "Received broadcast: $action. Rescheduling all active alarms.")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MediTimeDatabase.getDatabase(context)
                val medicines = db.dao().getActiveMedicines()
                val allSchedules = db.dao().getAllActiveSchedulesOnce()

                val now = System.currentTimeMillis()
                for (schedule in allSchedules) {
                    val med = db.dao().getMedicineById(schedule.medicineId)
                    if (med != null && med.isActive) {
                        val days = schedule.selectedDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                        val nextMillis = ReminderEngine.calculateNextTriggerMillis(
                            hour = schedule.timeHour,
                            minute = schedule.timeMinute,
                            selectedDays = days,
                            fromTimeMillis = now
                        )
                        ReminderEngine.scheduleAlarm(context, med, schedule, nextMillis)
                    }
                }
                Log.d("BootReceiver", "Successfully rescheduled ${allSchedules.size} active medicine alarms.")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms after boot/time change", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
