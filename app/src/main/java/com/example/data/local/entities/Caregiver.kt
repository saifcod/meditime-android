package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caregivers")
data class Caregiver(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val relationship: String = "Family",
    val contactPhone: String = "",
    val email: String = "",
    val hasConsent: Boolean = true,
    val notifyOnMissed: Boolean = true,
    val notes: String = ""
)
