package com.example.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.reminders.ReminderEngine

object NotificationHelper {
    const val CHANNEL_ID_REMINDERS = "meditime_reminders_channel_v2"
    const val CHANNEL_ID_MISSED = "meditime_missed_channel_v1"

    const val ACTION_DOSE_TAKEN = "com.example.meditime.ACTION_DOSE_TAKEN"
    const val ACTION_DOSE_SNOOZE = "com.example.meditime.ACTION_DOSE_SNOOZE"
    const val ACTION_DOSE_SKIP = "com.example.meditime.ACTION_DOSE_SKIP"
    const val ACTION_STOP_ALARM = "com.example.meditime.ACTION_STOP_ALARM"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Channel 1: High Priority Medicine Reminders with Alarm Attributes
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 1000)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Channel 2: Missed Dose Warnings
            val missedChannel = NotificationChannel(
                CHANNEL_ID_MISSED,
                "Missed Dose Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Follow-up notices for unrecorded medication doses"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(missedChannel)
        }
    }

    fun showDoseReminderNotification(
        context: Context,
        medicineId: Long,
        scheduleId: Long,
        scheduledTimeMillis: Long,
        medicineName: String,
        doseInstruction: String,
        foodInstruction: String,
        isSeniorMode: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Tap opens app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: TAKEN (Stops alarm + marks dose Taken)
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DOSE_TAKEN
            putExtra(ReminderEngine.EXTRA_MEDICINE_ID, medicineId)
            putExtra(ReminderEngine.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, scheduledTimeMillis)
            putExtra(ReminderEngine.EXTRA_MEDICINE_NAME, medicineName)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: SNOOZE 10 MIN (Stops current alarm + schedules 10m reminder)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DOSE_SNOOZE
            putExtra(ReminderEngine.EXTRA_MEDICINE_ID, medicineId)
            putExtra(ReminderEngine.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, scheduledTimeMillis)
            putExtra(ReminderEngine.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(ReminderEngine.EXTRA_DOSE_INSTRUCTION, doseInstruction)
            putExtra(ReminderEngine.EXTRA_FOOD_INSTRUCTION, foodInstruction)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: SKIP (Stops alarm + marks dose Skipped)
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DOSE_SKIP
            putExtra(ReminderEngine.EXTRA_MEDICINE_ID, medicineId)
            putExtra(ReminderEngine.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, scheduledTimeMillis)
            putExtra(ReminderEngine.EXTRA_MEDICINE_NAME, medicineName)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10 + 3).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 4: STOP ALARM (Stops sound without recording Taken yet)
        val stopAlarmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra(ReminderEngine.EXTRA_MEDICINE_ID, medicineId)
            putExtra(ReminderEngine.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ReminderEngine.EXTRA_SCHEDULED_TIME_MILLIS, scheduledTimeMillis)
            putExtra(ReminderEngine.EXTRA_MEDICINE_NAME, medicineName)
        }
        val stopAlarmPendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId * 10 + 4).toInt(),
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val instructionText = if (foodInstruction.isNotBlank()) {
            "$doseInstruction • $foodInstruction"
        } else {
            doseInstruction
        }

        val titleText = if (isSeniorMode) {
            "⏰ TIME TO TAKE: $medicineName"
        } else {
            "Time to take $medicineName"
        }

        val bigText = if (isSeniorMode) {
            "MEDICINE ALARM: $medicineName\nDOSE: $instructionText\nPlease take your medicine now or press Snooze."
        } else {
            "$medicineName\n$instructionText\nScheduled dosage reminder."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleText)
            .setContentText(instructionText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true) // Kept ongoing while alarm rings
            .setAutoCancel(false)
            .addAction(0, "✓ TAKEN", takenPendingIntent)
            .addAction(0, "⏰ SNOOZE 10m", snoozePendingIntent)
            .addAction(0, "✕ SKIP", skipPendingIntent)
            .addAction(0, "⏹ STOP ALARM", stopAlarmPendingIntent)
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)
    }
}
