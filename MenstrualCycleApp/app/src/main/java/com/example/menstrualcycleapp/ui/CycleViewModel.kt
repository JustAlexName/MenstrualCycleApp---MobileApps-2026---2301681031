package com.example.menstrualcycleapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.menstrualcycleapp.data.database.AppDatabase
import com.example.menstrualcycleapp.data.repository.CycleRepository
import com.example.menstrualcycleapp.model.CycleEntry
import com.example.menstrualcycleapp.model.SymptomLog
import kotlinx.coroutines.launch

class CycleViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CycleRepository(AppDatabase.get(app))

    val allCycles: LiveData<List<CycleEntry>> = repo.allCycles

    private val _nextPeriod  = MutableLiveData<Long?>()
    val nextPeriod: LiveData<Long?> = _nextPeriod

    private val _ovulation  = MutableLiveData<Long?>()
    val ovulation: LiveData<Long?> = _ovulation

    private val _avgCycle   = MutableLiveData(28.0)
    val avgCycle: LiveData<Double> = _avgCycle

    private val _avgPeriod  = MutableLiveData(5.0)
    val avgPeriod: LiveData<Double> = _avgPeriod

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    init { refreshStats() }

    // ---- Cycle CRUD ----
    fun addCycle(entry: CycleEntry) = viewModelScope.launch {
        repo.insert(entry)
        _toast.postValue("Цикълът е записан")
        refreshStats()
    }

    fun updateCycle(entry: CycleEntry) = viewModelScope.launch {
        repo.update(entry)
        _toast.postValue("Цикълът е обновен")
        refreshStats()
    }

    fun deleteCycle(entry: CycleEntry) = viewModelScope.launch {
        repo.delete(entry)
        _toast.postValue("Цикълът е изтрит")
        refreshStats()
    }

    fun endActiveCycle(endDate: Long) = viewModelScope.launch {
        val active = repo.getActive() ?: return@launch
        val days = ((endDate - active.startDate) / 86_400_000).toInt() + 1
        repo.update(active.copy(endDate = endDate, cycleLengthDays = days))
        _toast.postValue("Краят е записан")
        refreshStats()
    }

    // ---- Symptoms ----
    fun symptomsForDate(date: Long) = repo.symptomsForDate(date)

    suspend fun getSymptomsForDateSync(date: Long) = repo.symptomsForDateSync(date)

    fun saveSymptoms(date: Long, types: Set<String>) = viewModelScope.launch {
        repo.deleteSymptomsByDate(date)
        types.forEach { repo.insertSymptom(SymptomLog(date = date, symptomType = it)) }
        _toast.postValue("Симптомите са запазени")
    }

    // ---- Stats ----
    fun refreshStats() = viewModelScope.launch {
        _avgCycle.postValue(repo.avgCycleLength())
        _avgPeriod.postValue(repo.avgPeriodLength())
        _nextPeriod.postValue(repo.predictNext())
        _ovulation.postValue(repo.predictOvulation())
    }

    suspend fun getAllCyclesSync() = repo.getAllSync()
    suspend fun getAllSymptomsSync() = repo.allSymptomsSync()
    suspend fun getActive() = repo.getActive()
}
