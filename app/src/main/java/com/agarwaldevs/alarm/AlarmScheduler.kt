package com.agarwaldevs.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.*

class AlarmScheduler {
    companion object {
        private const val REMINDER_OFFSET_MILLIS = 5 * 60 * 1000

        fun scheduleAlarm(context: Context, alarm: Alarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmTime = getTimeInMillis(alarm.time)
            val reminderTime = alarmTime - REMINDER_OFFSET_MILLIS

            val adjustedAlarmTime = adjustForPastTime(alarmTime)
            val adjustedReminderTime = adjustForPastTime(reminderTime)

            // Reminders
            val reminderIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarm.id)
                putExtra("alarm_time", alarm.time)
                putExtra("notification_type", "reminder")
            }
            val reminderPendingIntent = PendingIntent.getBroadcast(
                context, alarm.id * 100 + 1, reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                adjustedReminderTime,
                reminderPendingIntent
            )

            // Alerts
            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarm.id)
                putExtra("name", alarm.name)
                putExtra("notification_type", "alarm")
                putExtra("ringtone", alarm.ringtone)
            }
            val alarmPendingIntent = PendingIntent.getBroadcast(
                context, alarm.id * 100, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                adjustedAlarmTime,
                alarmPendingIntent
            )
        }

        fun cancelAlarm(context: Context, alarmId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val alarmPendingIntent = PendingIntent.getBroadcast(
                context, alarmId * 100, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(alarmPendingIntent)

            val reminderIntent = Intent(context, AlarmReceiver::class.java)
            val reminderPendingIntent = PendingIntent.getBroadcast(
                context, alarmId * 100 + 1, reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(reminderPendingIntent)
        }

        fun snoozeAlarm(context: Context, alarmId: Int, minutes: Int) {
            val alarm = AlarmDatabaseHelper(context).getAlarm(alarmId)
            alarm?.let {
                val snoozeTime = System.currentTimeMillis() + minutes * 60 * 1000

                val newTime = Calendar.getInstance().apply {
                    timeInMillis = snoozeTime
                }.let { calendar ->
                    String.format(
                        "%02d:%02d",
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)
                    )
                }

                val snoozedAlarm = it.copy(time = newTime)
                scheduleAlarm(context, snoozedAlarm)
            }
        }

        fun createReminderNotification(context: Context, alarmId: Int, alarmTime: String) {
            val cancelIntent = PendingIntent.getBroadcast(
                context,
                alarmId * 100 + 2,
                Intent(context, AlarmReceiver::class.java).apply {
                    action = Notifier.ACTION_CANCEL_ALARM
                    putExtra("alarm_id", alarmId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, Notifier.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Reminder")
                .setContentText("Your alarm is set for $alarmTime")
                .addAction(R.drawable.ic_cancel, "Cancel", cancelIntent)
                .setAutoCancel(true)
                .build()

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                alarmId, notification
            )
        }

        fun createAlertNotification(context: Context, alarmId: Int) {
            val snoozeIntent = PendingIntent.getBroadcast(
                context,
                alarmId * 100 + 1,
                Intent(context, AlarmReceiver::class.java).apply {
                    action = Notifier.ACTION_SNOOZE_ALARM
                    putExtra("alarm_id", alarmId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = PendingIntent.getBroadcast(
                context,
                alarmId * 100 + 2,
                Intent(context, AlarmReceiver::class.java).apply {
                    action = Notifier.ACTION_STOP_ALARM
                    putExtra("alarm_id", alarmId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, Notifier.ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm")
                .setContentText("Your alarm is ringing!")
                .addAction(R.drawable.ic_snooze, "Snooze", snoozeIntent)
                .addAction(R.drawable.ic_cancel, "Stop", stopIntent)
                .setAutoCancel(true)
                .build()

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                alarmId, notification
            )
        }


        private fun getTimeInMillis(time: String): Long {
            val calendar = Calendar.getInstance()
            val parts = time.split(":")
            calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            calendar.set(Calendar.MINUTE, parts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        private fun adjustForPastTime(timeInMillis: Long): Long {
            val currentTime = System.currentTimeMillis()
            return if (timeInMillis < currentTime) {
                timeInMillis + 24 * 60 * 60 * 1000
            } else {
                timeInMillis
            }
        }
    }
}
