package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.Caregiver
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Prescription
import com.example.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface MediTimeDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // --- Medicines ---
    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY isActive DESC, name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)

    @Query("UPDATE medicines SET stockQuantity = :newQuantity WHERE id = :id")
    suspend fun updateMedicineStock(id: Long, newQuantity: Int)

    // --- Medication Schedules ---
    @Query("SELECT * FROM medication_schedules WHERE medicineId = :medicineId AND isEnabled = 1")
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<MedicationSchedule>>

    @Query("SELECT * FROM medication_schedules WHERE medicineId = :medicineId")
    suspend fun getSchedulesForMedicineOnce(medicineId: Long): List<MedicationSchedule>

    @Query("SELECT * FROM medication_schedules WHERE isEnabled = 1")
    suspend fun getAllActiveSchedulesOnce(): List<MedicationSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MedicationSchedule>)

    @Query("DELETE FROM medication_schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesForMedicine(medicineId: Long)

    @Query("DELETE FROM medication_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Long)

    // --- Dose Records ---
    @Query("SELECT * FROM dose_records WHERE scheduledTimeMillis BETWEEN :startTimeMillis AND :endTimeMillis ORDER BY scheduledTimeMillis ASC")
    fun getDoseRecordsForPeriod(startTimeMillis: Long, endTimeMillis: Long): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records WHERE scheduledTimeMillis BETWEEN :startTimeMillis AND :endTimeMillis ORDER BY scheduledTimeMillis ASC")
    suspend fun getDoseRecordsForPeriodOnce(startTimeMillis: Long, endTimeMillis: Long): List<DoseRecord>

    @Query("SELECT * FROM dose_records WHERE medicineId = :medicineId AND scheduleId = :scheduleId AND scheduledTimeMillis = :scheduledTimeMillis LIMIT 1")
    suspend fun findDoseRecord(medicineId: Long, scheduleId: Long, scheduledTimeMillis: Long): DoseRecord?

    @Query("SELECT * FROM dose_records ORDER BY scheduledTimeMillis DESC LIMIT 150")
    fun getRecentHistory(): Flow<List<DoseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseRecord(record: DoseRecord): Long

    @Update
    suspend fun updateDoseRecord(record: DoseRecord)

    @Query("UPDATE dose_records SET status = :status, actionTimeMillis = :actionTime, notes = :notes WHERE id = :id")
    suspend fun updateDoseStatus(id: Long, status: String, actionTime: Long, notes: String = "")

    // --- Prescriptions ---
    @Query("SELECT * FROM prescriptions ORDER BY prescriptionDateMillis DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: Long)

    // --- Caregivers ---
    @Query("SELECT * FROM caregivers ORDER BY name ASC")
    fun getAllCaregivers(): Flow<List<Caregiver>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(caregiver: Caregiver): Long

    @Query("DELETE FROM caregivers WHERE id = :id")
    suspend fun deleteCaregiverById(id: Long)

    // --- Data Reset ---
    @Query("DELETE FROM dose_records")
    suspend fun clearDoseRecords()

    @Query("DELETE FROM medication_schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM medicines")
    suspend fun clearMedicines()

    @Query("DELETE FROM prescriptions")
    suspend fun clearPrescriptions()

    @Query("DELETE FROM caregivers")
    suspend fun clearCaregivers()

    @Transaction
    suspend fun deleteAllData() {
        clearDoseRecords()
        clearSchedules()
        clearMedicines()
        clearPrescriptions()
        clearCaregivers()
    }
}
