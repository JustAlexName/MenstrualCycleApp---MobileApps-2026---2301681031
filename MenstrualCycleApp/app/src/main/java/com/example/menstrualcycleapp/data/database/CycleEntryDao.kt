package com.example.menstrualcycleapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.menstrualcycleapp.model.CycleEntry

@Dao
interface CycleEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CycleEntry): Long

    @Update
    suspend fun update(entry: CycleEntry)

    @Delete
    suspend fun delete(entry: CycleEntry)

    @Query("SELECT * FROM cycle_entries ORDER BY startDate DESC")
    fun getAll(): LiveData<List<CycleEntry>>

    @Query("SELECT * FROM cycle_entries ORDER BY startDate DESC")
    suspend fun getAllSync(): List<CycleEntry>

    @Query("SELECT * FROM cycle_entries ORDER BY startDate DESC LIMIT 1")
    suspend fun getLast(): CycleEntry?

    @Query("SELECT * FROM cycle_entries WHERE endDate IS NULL ORDER BY startDate DESC LIMIT 1")
    suspend fun getActive(): CycleEntry?
}
