package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val brandName: String = "",
    val genericName: String = "",
    val medicineType: String = "Tablet", // Tablet, Capsule, Syrup, Liquid, Injection, Drops, Cream, Inhaler, Other
    val strength: String = "", // e.g. 500mg
    val doseInstruction: String = "1 Tablet", // e.g. 1 Tablet
    val foodInstruction: String = "After food", // Before food, After food, With food, Empty stomach, No specific instruction
    val notes: String = "",
    val startDateEpochDay: Long = 0L,
    val endDateEpochDay: Long? = null, // null means ongoing
    val stockQuantity: Int = 0,
    val stockThreshold: Int = 5,
    val isStockTrackingEnabled: Boolean = false,
    val prescriptionId: Long? = null,
    val isActive: Boolean = true,
    val colorHex: String = "#0D47A1",
    val createdAt: Long = System.currentTimeMillis()
)
