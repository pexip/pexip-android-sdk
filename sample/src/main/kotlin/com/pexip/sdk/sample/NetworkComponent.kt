package com.pexip.sdk.sample

import android.util.Log
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NodeResolver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkComponent {

    private val client by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor {
            Log.d("OkHttpClient", it)
        }
        // Don't use `BODY` here as it attempts to read the whole response.
        // This makes SSE unavailable, rendering app useless
        // See https://github.com/square/okhttp/issues/4298 for more info
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    val service by lazy { InfinityService.create(client) }
    val nodeResolver by lazy { NodeResolver.create() }
}
