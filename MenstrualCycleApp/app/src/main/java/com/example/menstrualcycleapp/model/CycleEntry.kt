package com.example.menstrualcycleapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_entries")
data class CycleEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: Long,
    val endDate: Long? = null,
    val cycleLengthDays: Int? = null,
    val flowIntensity: Int = 2,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
