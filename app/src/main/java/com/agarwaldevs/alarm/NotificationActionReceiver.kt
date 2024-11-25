package com.agarwaldevs.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmId = intent?.getIntExtra("alarmId", -1) ?: return

        when (intent.action) {
            Notifier.ACTION_CANCEL_ALARM -> {
                Toast.makeText(context, "Alarm $alarmId canceled", Toast.LENGTH_SHORT).show()
                cancelNotification(context, alarmId)
            }
            Notifier.ACTION_SNOOZE_ALARM -> {
                Toast.makeText(context, "Alarm $alarmId snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
                snoozeAlarm(context, alarmId)
            }
            Notifier.ACTION_STOP_ALARM -> {
                Toast.makeText(context, "Alarm $alarmId stopped", Toast.LENGTH_SHORT).show()
                stopAlarm(context, alarmId)
            }
        }
    }

    private fun cancelNotification(context: Context, alarmId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId)
    }

    private fun snoozeAlarm(context: Context, alarmId: Int) {
        AlarmScheduler.snoozeAlarm(context, alarmId, 5)
    }

    private fun stopAlarm(context: Context, alarmId: Int) {
        AlarmScheduler.cancelAlarm(context, alarmId)
    }
}
