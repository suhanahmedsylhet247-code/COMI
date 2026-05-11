package com.comi.reader.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service for managing comic chapter downloads.
 * Runs in the background and shows a notification with download progress.
 */
class DownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "comi_downloads"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.comi.reader.DOWNLOAD_START"
        const val ACTION_CANCEL = "com.comi.reader.DOWNLOAD_CANCEL"
        const val EXTRA_COMIC_ID = "comic_id"
        const val EXTRA_CHAPTER_ID = "chapter_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification("Downloading..."))
            }
            ACTION_CANCEL -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Comic download progress"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("COMI")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }
}
