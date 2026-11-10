package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.MediTimeDao
import com.example.data.local.entities.Caregiver
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Prescription
import com.example.data.local.entities.UserProfile

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE user_profiles ADD COLUMN nightMode INTEGER NOT NULL DEFAULT 0")
        } catch (ignored: Exception) {
            // Column may already exist or table created with schema v2
        }
    }
}

@Database(
    entities = [
        UserProfile::class,
        Medicine::class,
        MedicationSchedule::class,
        DoseRecord::class,
        Prescription::class,
        Caregiver::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MediTimeDatabase : RoomDatabase() {
    abstract fun dao(): MediTimeDao

    companion object {
        @Volatile
        private var INSTANCE: MediTimeDatabase? = null

        fun getDatabase(context: Context): MediTimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediTimeDatabase::class.java,
                    "meditime_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
