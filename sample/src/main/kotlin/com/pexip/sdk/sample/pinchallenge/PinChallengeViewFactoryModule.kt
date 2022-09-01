package com.pexip.sdk.sample.pinchallenge

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
object PinChallengeViewFactoryModule {

    @Provides
    @Singleton
    @IntoSet
    fun providePinChallengeViewFactory(): ViewFactory<*> =
        composeViewFactory<PinChallengeRendering> { rendering, _ ->
            PinChallengeScreen(rendering)
        }
}
