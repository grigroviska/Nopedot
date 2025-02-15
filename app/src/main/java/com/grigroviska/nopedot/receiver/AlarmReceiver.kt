package com.grigroviska.nopedot.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val taskName = intent.getStringExtra("TASK_NAME")
            val notificationId = intent.getLongExtra("NOTIFICATION_ID", 0L)
            if (!taskName.isNullOrEmpty() && notificationId !=0L) {
                showNotification(context, taskName, notificationId)
            }
        }
    }

    private fun showNotification(context: Context, taskName: String, notificationId: Long) {
        val intent = Intent(context, HomeScreen::class.java).apply {
            putExtra("OPEN_FRAGMENT", "Create_Task_Fragment")
            putExtra("TASK_NAME", taskName)
            putExtra("NOTIFICATION_ID", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val channelId = "TASK_REMINDER_CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Task Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri: Uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder")
            .setContentText("Postponed Task: $taskName")
            .setSmallIcon(R.drawable.calendar)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(notificationId.toInt(), notificationBuilder.build())
    }
}
