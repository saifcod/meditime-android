package com.example.data.repository

import com.example.data.local.dao.MediTimeDao
import com.example.data.local.entities.Caregiver
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Prescription
import com.example.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

class MediTimeRepository(private val dao: MediTimeDao) {

    // User Profile
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()
    suspend fun saveUserProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    // Medicines
    val activeMedicines: Flow<List<Medicine>> = dao.getActiveMedicines()
    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()
    suspend fun getMedicineById(id: Long): Medicine? = dao.getMedicineById(id)
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?> = dao.getMedicineByIdFlow(id)
    suspend fun insertMedicine(medicine: Medicine): Long = dao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: Medicine) = dao.updateMedicine(medicine)
    suspend fun deleteMedicine(id: Long) = dao.deleteMedicineById(id)
    suspend fun updateStock(id: Long, newStock: Int) = dao.updateMedicineStock(id, newStock)

    // Schedules
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<MedicationSchedule>> =
        dao.getSchedulesForMedicine(medicineId)
    suspend fun getSchedulesForMedicineOnce(medicineId: Long): List<MedicationSchedule> =
        dao.getSchedulesForMedicineOnce(medicineId)
    suspend fun getAllActiveSchedulesOnce(): List<MedicationSchedule> =
        dao.getAllActiveSchedulesOnce()
    suspend fun insertSchedule(schedule: MedicationSchedule): Long =
        dao.insertSchedule(schedule)
    suspend fun insertSchedules(schedules: List<MedicationSchedule>) =
        dao.insertSchedules(schedules)
    suspend fun deleteSchedulesForMedicine(medicineId: Long) =
        dao.deleteSchedulesForMedicine(medicineId)
    suspend fun deleteScheduleById(scheduleId: Long) =
        dao.deleteScheduleById(scheduleId)

    // Dose Records
    fun getDoseRecordsForPeriod(startTimeMillis: Long, endTimeMillis: Long): Flow<List<DoseRecord>> =
        dao.getDoseRecordsForPeriod(startTimeMillis, endTimeMillis)
    suspend fun getDoseRecordsForPeriodOnce(startTimeMillis: Long, endTimeMillis: Long): List<DoseRecord> =
        dao.getDoseRecordsForPeriodOnce(startTimeMillis, endTimeMillis)
    suspend fun findDoseRecord(medicineId: Long, scheduleId: Long, timeMillis: Long): DoseRecord? =
        dao.findDoseRecord(medicineId, scheduleId, timeMillis)
    val recentHistory: Flow<List<DoseRecord>> = dao.getRecentHistory()
    suspend fun insertDoseRecord(record: DoseRecord): Long = dao.insertDoseRecord(record)
    suspend fun updateDoseRecord(record: DoseRecord) = dao.updateDoseRecord(record)
    suspend fun updateDoseStatus(id: Long, status: String, actionTime: Long, notes: String = "") =
        dao.updateDoseStatus(id, status, actionTime, notes)

    // Prescriptions
    val allPrescriptions: Flow<List<Prescription>> = dao.getAllPrescriptions()
    suspend fun insertPrescription(prescription: Prescription): Long = dao.insertPrescription(prescription)
    suspend fun deletePrescription(id: Long) = dao.deletePrescriptionById(id)

    // Caregivers
    val allCaregivers: Flow<List<Caregiver>> = dao.getAllCaregivers()
    suspend fun insertCaregiver(caregiver: Caregiver): Long = dao.insertCaregiver(caregiver)
    suspend fun deleteCaregiver(id: Long) = dao.deleteCaregiverById(id)

    // Data wipe
    suspend fun clearAllData() = dao.deleteAllData()
}
