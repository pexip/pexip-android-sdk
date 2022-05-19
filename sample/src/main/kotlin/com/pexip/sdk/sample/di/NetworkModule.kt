package com.pexip.sdk.sample.di

import android.util.Log
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NodeResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor {
            Log.d("OkHttpClient", it)
        }
        // Don't use `BODY` here as it attempts to read the whole response.
        // This makes SSE unavailable, rendering app useless
        // See https://github.com/square/okhttp/issues/4298 for more info
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideNodeResolver(): NodeResolver = NodeResolver.create()

    @Provides
    @Singleton
    fun OkHttpClient.provideInfinityService(): InfinityService = InfinityService.create(this)
}
