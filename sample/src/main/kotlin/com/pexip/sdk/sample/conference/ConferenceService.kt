package com.pexip.sdk.sample.conference

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pexip.sdk.sample.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConferenceService : Service() {

    @Inject
    lateinit var manager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_IMPORTANCE
        )
            .setName("Conference")
            .build()
        manager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle("Conference")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder = Binder()

    private companion object {

        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "conference"
        const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_DEFAULT
    }
}
