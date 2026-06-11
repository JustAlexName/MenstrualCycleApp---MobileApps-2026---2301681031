package com.example.menstrualcycleapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.menstrualcycleapp.model.SymptomLog

@Dao
interface SymptomLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SymptomLog): Long

    @Delete
    suspend fun delete(log: SymptomLog)

    @Query("SELECT * FROM symptom_logs WHERE date = :date")
    fun getForDate(date: Long): LiveData<List<SymptomLog>>

    @Query("SELECT * FROM symptom_logs WHERE date = :date")
    suspend fun getForDateSync(date: Long): List<SymptomLog>

    @Query("SELECT * FROM symptom_logs ORDER BY date DESC")
    suspend fun getAllSync(): List<SymptomLog>

    @Query("DELETE FROM symptom_logs WHERE date = :date")
    suspend fun deleteForDate(date: Long)
}
