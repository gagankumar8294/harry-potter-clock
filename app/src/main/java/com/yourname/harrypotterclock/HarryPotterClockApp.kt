package com.yourname.harrypotterclock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class HarryPotterClockApp : Application() {
    companion object {
        const val CHANNEL_ID = "hp_clock_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Harry Potter Clock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Harry Potter clock active on your lock screen"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
