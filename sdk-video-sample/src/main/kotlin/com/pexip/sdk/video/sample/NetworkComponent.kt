package com.pexip.sdk.video.sample

import android.util.Log
import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.TokenRequester
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkComponent {

    val client by lazy {
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

    val nodeResolver by lazy {
        NodeResolver.Builder()
            .client(client)
            .build()
    }
    val tokenRequester by lazy {
        TokenRequester.Builder()
            .client(client)
            .build()
    }
}
