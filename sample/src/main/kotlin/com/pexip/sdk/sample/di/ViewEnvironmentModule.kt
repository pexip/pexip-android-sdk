package com.pexip.sdk.sample.di

import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewEnvironmentModule {

    @Provides
    @Singleton
    fun provideViewEnvironment(viewFactories: Set<@JvmSuppressWildcards ViewFactory<*>>): ViewEnvironment {
        val viewRegistry = ViewRegistry(*viewFactories.toTypedArray())
        return ViewEnvironment(mapOf(ViewRegistry to viewRegistry))
    }
}
