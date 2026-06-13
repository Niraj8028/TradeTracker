package com.wallstreet.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wallstreet.MainActivity
import com.wallstreet.R

object NotificationHelper {
    private const val CHANNEL_ID = "trade_reminder_channel"
    private const val CHANNEL_NAME = "Trade Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications to remind you to log your trades"
    const val NOTIFICATION_ID = 1001

    const val ACTION_LOG_TRADE = "com.wallstreet.ACTION_LOG_TRADE"
    const val EXTRA_ROUTE = "extra_route"
    const val ROUTE_LOG_TRADE = "home/log_trade"

    fun showTradeReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_ROUTE, ROUTE_LOG_TRADE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use default icon for now
            .setContentTitle("Have you logged your trades today?")
            .setContentText("Don't forget to keep your trading journal updated!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
