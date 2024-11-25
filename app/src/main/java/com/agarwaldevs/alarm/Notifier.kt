package com.agarwaldevs.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class Notifier {
    companion object {
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val ALARM_CHANNEL_ID = "alarm_channel"
        const val ACTION_CANCEL_ALARM = "com.agarwaldevs.alarm.ACTION_CANCEL_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.agarwaldevs.alarm.ACTION_SNOOZE_ALARM"
        const val ACTION_STOP_ALARM = "com.agarwaldevs.alarm.ACTION_STOP_ALARM"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val reminderChannel = NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "Alarm Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for reminders before the alarm"
                }

                val alarmChannel = NotificationChannel(
                    ALARM_CHANNEL_ID,
                    "Alarm Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for alarm alerts"
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(reminderChannel)
                notificationManager.createNotificationChannel(alarmChannel)
            }
        }
    }
}
