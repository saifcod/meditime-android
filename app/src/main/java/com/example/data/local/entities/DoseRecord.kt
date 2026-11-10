package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_records",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicineId"]),
        Index(value = ["scheduledTimeMillis"]),
        Index(value = ["status"])
    ]
)
data class DoseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicineId: Long,
    val scheduleId: Long,
    val scheduledTimeMillis: Long,
    val actionTimeMillis: Long? = null,
    val status: String, // SCHEDULED, TAKEN, SKIPPED, MISSED, SNOOZED
    val doseInstruction: String = "",
    val foodInstruction: String = "",
    val notes: String = "",
    val medicineNameSnapshot: String = ""
)
