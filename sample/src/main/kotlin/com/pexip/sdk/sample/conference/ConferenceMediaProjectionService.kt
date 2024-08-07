/*
 * Copyright 2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.sample.conference

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.pexip.sdk.sample.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConferenceMediaProjectionService : Service() {

    @Inject
    lateinit var manager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannelCompat.Builder(
            getString(R.string.conference_notification_channel_id),
            NOTIFICATION_CHANNEL_IMPORTANCE,
        )
            .setName(getString(R.string.conference_notification_channel_name))
            .build()
        manager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(this, notificationChannel.id)
            .setOngoing(true)
            .setContentTitle(getString(R.string.conference_media_projection_notification_content_title))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        val serviceType =
            if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder = Binder()

    private companion object {

        const val NOTIFICATION_ID = 2
        const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_DEFAULT
    }
}
