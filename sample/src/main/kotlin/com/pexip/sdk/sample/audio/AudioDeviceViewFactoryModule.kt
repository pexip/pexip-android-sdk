package com.pexip.sdk.sample.audio

import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.compose.composeViewFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioDeviceViewFactoryModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideAudioDeviceViewFactory(): ViewFactory<*> =
        composeViewFactory<AudioDeviceRendering> { rendering, _ ->
            AudioDeviceDialog(rendering)
        }
}
