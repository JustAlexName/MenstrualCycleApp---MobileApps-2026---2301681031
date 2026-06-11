package com.example.menstrualcycleapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_logs")
data class SymptomLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val symptomType: String,
    val intensity: Int = 1,
    val note: String = ""
)

object SymptomTypes {
    const val HEADACHE        = "headache"
    const val CRAMPS          = "cramps"
    const val BLOATING        = "bloating"
    const val MOOD_SWINGS     = "mood_swings"
    const val FATIGUE         = "fatigue"
    const val BREAST_TENDER   = "breast_tenderness"
    const val ACNE            = "acne"
    const val NAUSEA          = "nausea"
    const val BACK_PAIN       = "back_pain"
    const val INSOMNIA        = "insomnia"

    val ALL = listOf(HEADACHE, CRAMPS, BLOATING, MOOD_SWINGS, FATIGUE,
        BREAST_TENDER, ACNE, NAUSEA, BACK_PAIN, INSOMNIA)

    fun label(type: String) = when (type) {
        HEADACHE      -> "🤕 Главоболие"
        CRAMPS        -> "😣 Спазми"
        BLOATING      -> "😮 Подуване"
        MOOD_SWINGS   -> "😤 Промени в настроението"
        FATIGUE       -> "😴 Умора"
        BREAST_TENDER -> "💔 Чувствителни гърди"
        ACNE          -> "😖 Акне"
        NAUSEA        -> "🤢 Гадене"
        BACK_PAIN     -> "🔙 Болки в гърба"
        INSOMNIA      -> "😶 Безсъние"
        else          -> type
    }
}
