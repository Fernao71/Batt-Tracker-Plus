package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Query("SELECT * FROM battery_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<BatteryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BatteryRecord)

    @Query("DELETE FROM battery_history WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM battery_history")
    suspend fun clearHistory()
}
