package com.agarwaldevs.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val alarmTime = intent.getIntExtra("alarm_time", -1)
        val ringtoneUri = intent.getStringExtra("ringtone")

        when (action) {
            Notifier.ACTION_SNOOZE_ALARM -> {
                val snoozeMinutes = 5
                AlarmScheduler.snoozeAlarm(context, alarmId, snoozeMinutes)
                stopRingtone()
            }

            Notifier.ACTION_STOP_ALARM -> {
                stopRingtone()
                disableAlarm(context, alarmId)
                cancelNotification(context, alarmId)
            }

            Notifier.ACTION_CANCEL_ALARM -> {
                AlarmScheduler.cancelAlarm(context, alarmId)
                stopRingtone()
                disableAlarm(context, alarmId)
                cancelNotification(context, alarmId)
            }

            else -> {
                val notificationType = intent.getStringExtra("notification_type")
                val alarmName = intent.getStringExtra("name") ?: "Alarm"

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {

                    if (notificationType == "reminder") {
                        AlarmScheduler.createReminderNotification(context, alarmId, alarmName)
                    } else if (notificationType == "alarm") {
                        AlarmScheduler.createAlertNotification(context, alarmId)
                        playRingtone(context, ringtoneUri)
                    }
                } else {
                    Log.w("AlarmReceiver", "POST_NOTIFICATIONS permission not granted.")
                }
            }
        }
    }

    private fun playRingtone(context: Context, ringtoneUri: String?) {
        try {
            stopRingtone()
            mediaPlayer = MediaPlayer.create(context, Uri.parse(ringtoneUri))
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to play ringtone: ${e.message}")
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error stopping MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private fun disableAlarm(context: Context, alarmId: Int) {
        val dbHelper = AlarmDatabaseHelper(context)
        val alarm = dbHelper.getAlarm(alarmId)
        alarm?.let {
            dbHelper.toggleAlarm(alarmId, false)
            alarm.isEnabled = false
        }
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
