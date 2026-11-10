package com.example

import com.example.core.reminders.ReminderEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

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
}
