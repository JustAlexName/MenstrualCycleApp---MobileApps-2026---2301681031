package com.example.menstrualcycleapp.data.repository

import androidx.lifecycle.LiveData
import com.example.menstrualcycleapp.data.database.AppDatabase
import com.example.menstrualcycleapp.model.CycleEntry
import com.example.menstrualcycleapp.model.SymptomLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CycleRepository(db: AppDatabase) {

    private val cycleDao = db.cycleEntryDao()
    private val symptomDao = db.symptomLogDao()

    // ---- Cycle ----
    val allCycles: LiveData<List<CycleEntry>> = cycleDao.getAll()

    suspend fun insert(e: CycleEntry) = withContext(Dispatchers.IO) { cycleDao.insert(e) }
    suspend fun update(e: CycleEntry) = withContext(Dispatchers.IO) { cycleDao.update(e) }
    suspend fun delete(e: CycleEntry) = withContext(Dispatchers.IO) { cycleDao.delete(e) }
    suspend fun getLast() = withContext(Dispatchers.IO) { cycleDao.getLast() }
    suspend fun getActive() = withContext(Dispatchers.IO) { cycleDao.getActive() }
    suspend fun getAllSync() = withContext(Dispatchers.IO) { cycleDao.getAllSync() }

    // ---- Symptoms ----
    fun symptomsForDate(date: Long): LiveData<List<SymptomLog>> = symptomDao.getForDate(date)
    suspend fun symptomsForDateSync(date: Long) = withContext(Dispatchers.IO) { symptomDao.getForDateSync(date) }
    suspend fun insertSymptom(s: SymptomLog) = withContext(Dispatchers.IO) { symptomDao.insert(s) }
    suspend fun deleteSymptom(s: SymptomLog) = withContext(Dispatchers.IO) { symptomDao.delete(s) }
    suspend fun deleteSymptomsByDate(date: Long) = withContext(Dispatchers.IO) { symptomDao.deleteForDate(date) }
    suspend fun allSymptomsSync() = withContext(Dispatchers.IO) { symptomDao.getAllSync() }

    // ---- Stats ----
    suspend fun avgCycleLength(): Double {
        val entries = getAllSync()
        if (entries.size < 2) return 28.0
        val lengths = entries.zipWithNext { a, b ->
            ((a.startDate - b.startDate) / 86_400_000.0).toInt()
        }.filter { it in 15..60 }
        return if (lengths.isEmpty()) 28.0 else lengths.average()
    }

    suspend fun avgPeriodLength(): Double {
        val entries = getAllSync().filter { it.endDate != null }
        if (entries.isEmpty()) return 5.0
        return entries.map { ((it.endDate!! - it.startDate) / 86_400_000.0).toInt() + 1 }.average()
    }

    suspend fun predictNext(): Long? {
        val last = getLast() ?: return null
        return last.startDate + (avgCycleLength() * 86_400_000).toLong()
    }

    suspend fun predictOvulation(): Long? {
        val next = predictNext() ?: return null
        return next - (avgCycleLength() / 2 * 86_400_000).toLong()
    }
}
