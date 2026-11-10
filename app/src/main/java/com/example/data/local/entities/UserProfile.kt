package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val timeFormat24Hour: Boolean = false,
    val seniorMode: Boolean = false,
    val nightMode: Boolean = false, // Default is false (Light mode default as requested)
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 10,
    val voiceRemindersEnabled: Boolean = true,
    val isOnboarded: Boolean = false
)
