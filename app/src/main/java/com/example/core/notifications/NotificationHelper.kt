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
    const val CHANNEL_ID_REMINDERS = "meditime_reminders_channel_v1"
    const val CHANNEL_ID_MISSED = "meditime_missed_channel_v1"

    const val ACTION_DOSE_TAKEN = "com.example.meditime.ACTION_DOSE_TAKEN"
    const val ACTION_DOSE_SNOOZE = "com.example.meditime.ACTION_DOSE_SNOOZE"
    const val ACTION_DOSE_SKIP = "com.example.meditime.ACTION_DOSE_SKIP"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Channel 1: High Priority Medicine Reminders
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
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
                setSound(soundUri, audioAttributes)
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
        foodInstruction: String
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

        // Action: Taken
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

        // Action: Snooze 10m
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

        // Action: Skip
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

        val instructionText = if (foodInstruction.isNotBlank()) {
            "$doseInstruction • $foodInstruction"
        } else {
            doseInstruction
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take $medicineName")
            .setContentText(instructionText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$medicineName\n$instructionText\nScheduled dosage reminder."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "✓ Taken", takenPendingIntent)
            .addAction(0, "⏰ Snooze 10m", snoozePendingIntent)
            .addAction(0, "✕ Skip", skipPendingIntent)
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)
    }
}
