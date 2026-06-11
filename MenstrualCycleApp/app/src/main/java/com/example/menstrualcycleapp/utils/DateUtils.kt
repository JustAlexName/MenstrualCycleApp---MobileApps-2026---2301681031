package com.example.menstrualcycleapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val short = SimpleDateFormat("dd MMM", Locale.getDefault())

    fun format(ts: Long): String = fmt.format(Date(ts))
    fun formatShort(ts: Long): String = short.format(Date(ts))

    fun todayMidnight(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun daysUntil(ts: Long) = ((ts - System.currentTimeMillis()) / 86_400_000).toInt()
    fun daysBetween(from: Long, to: Long) = ((to - from) / 86_400_000).toInt()
}
