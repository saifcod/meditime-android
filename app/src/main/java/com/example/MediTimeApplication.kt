package com.example

import android.app.Application
import android.util.Log
import com.example.core.notifications.NotificationHelper
import com.example.data.local.MediTimeDatabase
import com.example.data.repository.MediTimeRepository

class MediTimeApplication : Application() {

    val database by lazy { MediTimeDatabase.getDatabase(this) }
    val repository by lazy { MediTimeRepository(database.dao()) }

    override fun onCreate() {
        super.onCreate()
        try {
            // Safe initialize notification channels
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.e("MediTimeApp", "Error during initialization", e)
        }
    }
}
