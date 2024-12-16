package com.atarusov.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.atarusov.musicplayer.service.PlayerService

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            PlayerService.CHANNEL_ID,
            "Player Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}