package com.cineverse.android.features.details

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: "CineVerse Movie"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "cineverse_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Movie Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "CineVerse Watchlist local alarms notification channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Movie Night Reminder!")
            .setContentText("Time to sit back and watch '$movieTitle'!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
            
        notificationManager.notify(movieTitle.hashCode(), notification)
    }
}
