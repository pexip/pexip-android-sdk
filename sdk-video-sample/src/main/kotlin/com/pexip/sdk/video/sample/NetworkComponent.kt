package com.pexip.sdk.video.sample

import android.util.Log
import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.TokenRequester
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkComponent {

    private val client by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor {
            Log.d("OkHttpClient", it)
        }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
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
