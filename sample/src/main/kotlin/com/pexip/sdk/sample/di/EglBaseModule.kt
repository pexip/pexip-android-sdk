package com.pexip.sdk.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.EglBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EglBaseModule {

    @Provides
    @Singleton
    fun provideEglBase(): EglBase = EglBase.create()
}
