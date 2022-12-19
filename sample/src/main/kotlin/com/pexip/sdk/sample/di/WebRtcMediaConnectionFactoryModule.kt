package com.pexip.sdk.sample.di

import android.app.Application
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.EglBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRtcMediaConnectionFactoryModule {

    @Provides
    @Singleton
    fun Application.provideMediaConnectionFactory(eglBase: EglBase) =
        WebRtcMediaConnectionFactory.Builder(this)
            .eglBase(eglBase)
            .build()
}
