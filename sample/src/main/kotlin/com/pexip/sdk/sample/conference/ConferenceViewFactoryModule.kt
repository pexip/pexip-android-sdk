package com.pexip.sdk.sample.conference

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
object ConferenceViewFactoryModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideConferenceCallViewFactory(): ViewFactory<*> =
        composeViewFactory<ConferenceCallRendering> { rendering, _ ->
            ConferenceCallScreen(rendering)
        }

    @Provides
    @Singleton
    @IntoSet
    fun provideConferenceEventsViewFactory(): ViewFactory<*> =
        composeViewFactory<ConferenceEventsRendering> { rendering, _ ->
            ConferenceEventsScreen(rendering)
        }
}
