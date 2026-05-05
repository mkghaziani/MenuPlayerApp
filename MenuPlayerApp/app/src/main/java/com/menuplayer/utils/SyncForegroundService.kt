package com.menuplayer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.menuplayer.R

/**
 * Foreground service that keeps the process alive on memory-constrained TV devices.
 * Started by PlayerActivity; shows a minimal persistent notification.
 */
class SyncForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Menu Player",
                NotificationManager.IMPORTANCE_MIN
            ).apply { description = "Menu display service" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Menu Player Running")
            .setContentText("Displaying menu on ${PrefsManager.screenId}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    companion object {
        const val CHANNEL_ID = "menu_player_channel"
        const val NOTIFICATION_ID = 1001
    }
}
