package com.example.goalhabitapp.notifications

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val UNIQUE_NAME = "daily_reminder_work"

    fun scheduleDaily(context: Context, hour: Int = 20, minute: Int = 0) {
        NotificationHelper.ensureChannel(context)

        // хотим, чтобы первое уведомление было ближайшее в hour:minute
        val now = LocalDateTime.now()
        var next = now.with(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)

        val initialDelay = Duration.between(now, next).toMillis()

        val req = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(UNIQUE_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }
}