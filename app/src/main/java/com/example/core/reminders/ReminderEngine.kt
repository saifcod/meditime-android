package com.example.core.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Medicine
import java.util.Calendar

object ReminderEngine {
    private const val TAG = "ReminderEngine"
    const val ACTION_TRIGGER_REMINDER = "com.example.meditime.ACTION_TRIGGER_REMINDER"

    const val EXTRA_MEDICINE_ID = "extra_medicine_id"
    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    const val EXTRA_SCHEDULED_TIME_MILLIS = "extra_scheduled_time_millis"
    const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    const val EXTRA_DOSE_INSTRUCTION = "extra_dose_instruction"
    const val EXTRA_FOOD_INSTRUCTION = "extra_food_instruction"

    /**
     * Calculates the next trigger timestamp in milliseconds for a given schedule.
     */
    fun calculateNextTriggerMillis(
        hour: Int,
        minute: Int,
        selectedDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
        fromTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val now = Calendar.getInstance().apply { timeInMillis = fromTimeMillis }
        val target = Calendar.getInstance().apply {
            timeInMillis = fromTimeMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time today already passed or day not selected, find next valid day
        var daysAdded = 0
        while (daysAdded < 14) {
            val candidateMillis = target.timeInMillis
            val dayOfWeek = target.get(Calendar.DAY_OF_WEEK)
            // Calendar: SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
            // Convert to ISO 1=Mon..7=Sun
            val isoDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

            if (candidateMillis > fromTimeMillis && (selectedDays.isEmpty() || selectedDays.contains(isoDay))) {
                return candidateMillis
            }
            target.add(Calendar.DAY_OF_MONTH, 1)
            daysAdded++
        }
        return target.timeInMillis
    }

    /**
     * Schedules exact or inexact alarm based on Android API level and permissions.
     */
    fun scheduleAlarm(
        context: Context,
        medicine: Medicine,
        schedule: MedicationSchedule,
        targetMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_MEDICINE_ID, medicine.id)
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_SCHEDULED_TIME_MILLIS, targetMillis)
            putExtra(EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(EXTRA_DOSE_INSTRUCTION, medicine.doseInstruction)
            putExtra(EXTRA_FOOD_INSTRUCTION, medicine.foodInstruction)
        }

        // Generate stable unique requestCode from scheduleId
        val requestCode = (schedule.id.toInt() and 0xFFFF) xor (targetMillis.toInt() and 0xFFFF)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for ${medicine.name} at $targetMillis (code $requestCode)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed fallback alarm scheduling", fallbackEx)
            }
        }
    }

    /**
     * Cancels an alarm for a schedule.
     */
    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val requestCode = scheduleId.toInt() and 0xFFFF
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
