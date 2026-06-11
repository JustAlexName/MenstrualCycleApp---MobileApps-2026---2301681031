package com.example.menstrualcycleapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.menstrualcycleapp.R
import com.example.menstrualcycleapp.data.database.AppDatabase
import com.example.menstrualcycleapp.data.repository.CycleRepository
import com.example.menstrualcycleapp.ui.MainActivity

class PeriodCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val repo = CycleRepository(AppDatabase.get(applicationContext))
        val next = repo.predictNext() ?: return Result.success()
        val days = ((next - System.currentTimeMillis()) / 86_400_000).toInt()
        when (days) {
            3    -> notify("🩸 Менструацията наближава", "Очаква се след 3 дни.", 1002)
            1    -> notify("🩸 Менструацията е утре", "Подгответе се.", 1003)
            0    -> notify("🩸 Менструацията може да е днес", "Не забравяйте да отбележите началото.", 1004)
        }
        return Result.success()
    }

    private fun notify(title: String, text: String, id: Int) {
        val channelId = "period_channel"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Напомняния за цикъл", NotificationManager.IMPORTANCE_HIGH))
        }
        val pi = PendingIntent.getActivity(
            applicationContext, id,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(id, NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi).setAutoCancel(true).build())
    }
}
