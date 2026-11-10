package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val doctorName: String = "",
    val clinicName: String = "",
    val prescriptionDateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val attachmentPath: String? = null,
    val extractedOcrText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
