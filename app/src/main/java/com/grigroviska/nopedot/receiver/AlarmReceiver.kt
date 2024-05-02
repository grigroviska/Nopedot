package com.grigroviska.nopedot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.grigroviska.nopedot.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message")
        val reminder = intent.getStringExtra("reminder")
        val content = intent.getStringExtra("content")

        if (message != null && reminder != null && content != null) {
            //showNotification(context, message, reminder, content, "Reminder")
        }
    }

    /*private fun showNotification(context: Context, title: String, reminder: String, content: String, channelId: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.calendar_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }*/
}
