package com.example.goalhabitapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.show(
            applicationContext,
            title = "GoalHabitApp",
            text = "Не забудьте отметить прогресс по целям 👇"
        )
        return Result.success()
    }
}