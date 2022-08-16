package com.pexip.sdk.sample.di

import com.pexip.sdk.media.CameraVideoTrackFactory
import com.pexip.sdk.media.LocalAudioTrackFactory
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.android.MediaProjectionVideoTrackFactory
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MediaConnectionFactoryModule {

    @Binds
    fun WebRtcMediaConnectionFactory.bindMediaConnectionFactory(): MediaConnectionFactory

    @Binds
    fun WebRtcMediaConnectionFactory.bindLocalAudioTrackFactory(): LocalAudioTrackFactory

    @Binds
    fun WebRtcMediaConnectionFactory.bindCameraVideoTrackFactory(): CameraVideoTrackFactory

    @Binds
    fun WebRtcMediaConnectionFactory.bindMediaProjectionVideoTrackFactory(): MediaProjectionVideoTrackFactory
}
