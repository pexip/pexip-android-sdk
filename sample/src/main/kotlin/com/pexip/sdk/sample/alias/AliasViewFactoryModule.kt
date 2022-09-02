package com.pexip.sdk.sample.alias

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
object AliasViewFactoryModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideAliasViewFactory(): ViewFactory<*> =
        composeViewFactory<AliasRendering> { rendering, _ ->
            AliasScreen(rendering)
        }
}
