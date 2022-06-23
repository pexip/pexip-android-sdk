package com.pexip.sdk.sample.di

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationManagerCompatModule {

    @Provides
    @Singleton
    fun Application.provideNotificationManagerCompat(): NotificationManagerCompat =
        NotificationManagerCompat.from(this)
}
