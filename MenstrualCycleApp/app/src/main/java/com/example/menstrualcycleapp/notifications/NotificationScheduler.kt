package com.example.menstrualcycleapp.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)

        // Ежедневно напомняне в 20:00
        val delay = delayUntil(20, 0)
        val daily = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily")
            .build()
        wm.enqueueUniquePeriodicWork("daily", ExistingPeriodicWorkPolicy.UPDATE, daily)

        // Проверка за предстояща менструация
        val check = PeriodicWorkRequestBuilder<PeriodCheckWorker>(1, TimeUnit.DAYS)
            .addTag("period_check")
            .build()
        wm.enqueueUniquePeriodicWork("period_check", ExistingPeriodicWorkPolicy.UPDATE, check)
    }

    fun cancelAll(context: Context) = WorkManager.getInstance(context).cancelAllWork()

    private fun delayUntil(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis - now
    }
}
