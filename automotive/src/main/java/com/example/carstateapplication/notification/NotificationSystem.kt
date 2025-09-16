package com.example.carstateapplication.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.carstateapplication.R


object NotificationSystem {
    private const val TAG = "NotificationSystem"

    @SuppressLint("ObsoleteSdkInt")
    fun createNotificationChannel(context: Context, channelId: String?) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        context: Context,
        channelId: String?,
        title: String?,
        message: String?,
        intent: Intent?
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = Notification.Builder(context)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(
                PendingIntent.getBroadcastIntent(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
        manager.notify(0, notification)
    }
}