package com.example.goalhabitapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.goalhabitapp.R

object NotificationHelper {
    const val CHANNEL_ID = "daily_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Ежедневные напоминания про цели/привычки"
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    fun show(context: Context, title: String, text: String) {
        ensureChannel(context)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // можно заменить на свою иконку
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 100000).toInt(), notif)
    }
}