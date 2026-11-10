package com.example

import com.example.core.notifications.NotificationHelper
import com.example.core.reminders.ReminderEngine
import com.example.core.sound.AlarmPreferences
import com.example.core.sound.AlarmTone
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    // 1. Reminder trigger calculation for upcoming dose today
    @Test
    fun testReminderNextTriggerCalculation() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fromMillis = nowCal.timeInMillis

        // Schedule at 8:00 AM today
        val nextTrigger = ReminderEngine.calculateNextTriggerMillis(
            hour = 8,
            minute = 0,
            selectedDays = listOf(1, 2, 3, 4, 5, 6, 7),
            fromTimeMillis = fromMillis
        )

        val triggerCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(8, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
        assertTrue(nextTrigger > fromMillis)
    }

    // 2. Past time roll to tomorrow or next configured day
    @Test
    fun testReminderPastTimeNextDayRoll() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fromMillis = nowCal.timeInMillis

        // Schedule at 8:00 AM (already passed today) -> Should roll to tomorrow 8:00 AM
        val nextTrigger = ReminderEngine.calculateNextTriggerMillis(
            hour = 8,
            minute = 0,
            selectedDays = listOf(1, 2, 3, 4, 5, 6, 7),
            fromTimeMillis = fromMillis
        )

        assertTrue(nextTrigger > fromMillis)
        val triggerCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(8, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
    }

    // 3. Alarm Tone, Preferences Defaults, and Custom Audio Fields
    @Test
    fun testAlarmTonesAndPreferencesDefaults() {
        val prefs = AlarmPreferences()
        assertTrue("Audible alarm should be enabled by default", prefs.audibleAlarmEnabled)
        assertTrue("Vibration should be enabled by default", prefs.vibrationEnabled)
        assertEquals("Default tone should be CHIME", AlarmTone.CHIME, prefs.selectedTone)
        assertEquals("Default snooze should be 10 minutes", 10, prefs.snoozeDurationMinutes)
        assertEquals("Default max alarm duration should be 120s (2m)", 120, prefs.maxAlarmDurationSeconds)
        assertNull("Custom audio URI should default to null", prefs.customAudioUri)
        assertNull("Custom audio title should default to null", prefs.customAudioTitle)

        // Custom tone instantiation
        val customPrefs = prefs.copy(
            selectedTone = AlarmTone.CUSTOM_FILE,
            customAudioUri = "content://media/external/audio/media/123",
            customAudioTitle = "MyReminderSound.mp3",
            snoozeDurationMinutes = 15,
            maxAlarmDurationSeconds = 300
        )
        assertEquals(AlarmTone.CUSTOM_FILE, customPrefs.selectedTone)
        assertEquals("content://media/external/audio/media/123", customPrefs.customAudioUri)
        assertEquals("MyReminderSound.mp3", customPrefs.customAudioTitle)
        assertEquals(15, customPrefs.snoozeDurationMinutes)
        assertEquals(300, customPrefs.maxAlarmDurationSeconds)
    }

    // 4. Notification Action Constants & Channel Verification
    @Test
    fun testNotificationActionConstants() {
        assertEquals("com.example.meditime.ACTION_DOSE_TAKEN", NotificationHelper.ACTION_DOSE_TAKEN)
        assertEquals("com.example.meditime.ACTION_DOSE_SNOOZE", NotificationHelper.ACTION_DOSE_SNOOZE)
        assertEquals("com.example.meditime.ACTION_DOSE_SKIP", NotificationHelper.ACTION_DOSE_SKIP)
        assertEquals("com.example.meditime.ACTION_STOP_ALARM", NotificationHelper.ACTION_STOP_ALARM)
        assertNotNull(NotificationHelper.CHANNEL_ID_REMINDERS)
    }

    // 5. Dose Record Status Validation
    @Test
    fun testDoseRecordStatuses() {
        val dose = DoseRecord(
            medicineId = 101L,
            scheduleId = 201L,
            scheduledTimeMillis = System.currentTimeMillis(),
            status = "SCHEDULED",
            medicineNameSnapshot = "Amoxicillin"
        )
        assertEquals("SCHEDULED", dose.status)

        val takenDose = dose.copy(status = "TAKEN", actionTimeMillis = System.currentTimeMillis())
        assertEquals("TAKEN", takenDose.status)
        assertTrue((takenDose.actionTimeMillis ?: 0L) > 0)

        val skippedDose = dose.copy(status = "SKIPPED", actionTimeMillis = System.currentTimeMillis())
        assertEquals("SKIPPED", skippedDose.status)

        val snoozedDose = dose.copy(status = "SNOOZED")
        assertEquals("SNOOZED", snoozedDose.status)
    }

    // 6. Adherence Rate Calculation Logic
    @Test
    fun testAdherenceRateCalculation() {
        val doses = listOf(
            DoseRecord(medicineId = 1L, scheduleId = 1L, scheduledTimeMillis = 1000L, status = "TAKEN", medicineNameSnapshot = "Med A"),
            DoseRecord(medicineId = 1L, scheduleId = 2L, scheduledTimeMillis = 2000L, status = "TAKEN", medicineNameSnapshot = "Med A"),
            DoseRecord(medicineId = 1L, scheduleId = 3L, scheduledTimeMillis = 3000L, status = "SKIPPED", medicineNameSnapshot = "Med A"),
            DoseRecord(medicineId = 1L, scheduleId = 4L, scheduledTimeMillis = 4000L, status = "MISSED", medicineNameSnapshot = "Med A")
        )

        val takenCount = doses.count { it.status == "TAKEN" }
        val totalActionable = doses.count { it.status in listOf("TAKEN", "MISSED", "SKIPPED") }
        val adherenceRate = if (totalActionable > 0) ((takenCount.toFloat() / totalActionable) * 100).toInt() else 100

        assertEquals(2, takenCount)
        assertEquals(4, totalActionable)
        assertEquals(50, adherenceRate)
    }

    // 7. Medicine Inventory Tracking & Low Stock Detection
    @Test
    fun testMedicineInventoryAndLowStock() {
        val medicine = Medicine(
            name = "Metformin",
            strength = "500mg",
            isStockTrackingEnabled = true,
            stockQuantity = 4,
            stockThreshold = 5
        )

        val isLowStock = medicine.isStockTrackingEnabled && medicine.stockQuantity <= medicine.stockThreshold
        assertTrue("Medicine with 4 units and threshold 5 should trigger low stock alert", isLowStock)

        val refilledMed = medicine.copy(stockQuantity = 30)
        val isStillLow = refilledMed.isStockTrackingEnabled && refilledMed.stockQuantity <= refilledMed.stockThreshold
        assertFalse("Refilled medicine with 30 units should not be low stock", isStillLow)
    }

    // 8. User Profile Default Mode & Senior Mode
    @Test
    fun testUserProfileDefaultsAndModes() {
        val defaultProfile = UserProfile()
        assertFalse("Night mode should be false by default (Light Mode default)", defaultProfile.nightMode)
        assertFalse("Senior mode should be false by default (Standard Mode default)", defaultProfile.seniorMode)
        assertTrue("Sound should be enabled by default", defaultProfile.soundEnabled)
        assertTrue("Vibration should be enabled by default", defaultProfile.vibrationEnabled)

        val seniorProfile = defaultProfile.copy(seniorMode = true, nightMode = true, name = "Grandma Rose")
        assertTrue(seniorProfile.seniorMode)
        assertTrue(seniorProfile.nightMode)
        assertEquals("Grandma Rose", seniorProfile.name)
    }
}
