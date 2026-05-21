package com.example.data

import kotlinx.coroutines.flow.Flow

class BatteryRepository(private val batteryDao: BatteryDao) {
    val allRecords: Flow<List<BatteryRecord>> = batteryDao.getAllRecords()

    suspend fun insert(record: BatteryRecord) {
        batteryDao.insertRecord(record)
    }

    suspend fun deleteById(id: Int) {
        batteryDao.deleteRecordById(id)
    }

    suspend fun clearAll() {
        batteryDao.clearHistory()
    }
}
