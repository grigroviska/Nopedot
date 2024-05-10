package com.grigroviska.nopedot.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val taskName = intent.getStringExtra("TASK_NAME")
            if (taskName != null && taskName.isNotEmpty()) {
                showNotification(context, taskName)
            }
        }
    }

    private fun showNotification(context: Context, taskName: String) {
        val intent = Intent(context, HomeScreen::class.java).apply {
            putExtra("OPEN_FRAGMENT", "Task_Feed_Fragment")
            putExtra("TASK_NAME", taskName)
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
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Hatırlatma")
            .setContentText("Ertelenen göreviniz: $taskName")
            .setSmallIcon(R.drawable.calendar)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = 1
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}