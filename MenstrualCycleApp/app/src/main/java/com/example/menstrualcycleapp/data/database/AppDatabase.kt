package com.example.menstrualcycleapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.menstrualcycleapp.model.CycleEntry
import com.example.menstrualcycleapp.model.SymptomLog

@Database(entities = [CycleEntry::class, SymptomLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleEntryDao(): CycleEntryDao
    abstract fun symptomLogDao(): SymptomLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cycle_db")
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
