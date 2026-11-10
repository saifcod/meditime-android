package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicineId"])]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicineId: Long,
    val scheduleType: String = "DAILY", // DAILY, WEEKDAYS, INTERVAL
    val timeHour: Int, // 0-23
    val timeMinute: Int, // 0-59
    val selectedDaysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon..7=Sun
    val isEnabled: Boolean = true
)
