package com.pexip.sdk.sample.composer

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
object ComposerViewFactoryModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideComposerViewFactory(): ViewFactory<*> =
        composeViewFactory<ComposerRendering> { rendering, _ ->
            Composer(rendering)
        }
}
