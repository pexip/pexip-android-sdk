package com.pexip.sdk.video.sample

import android.util.Log
import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.api.InfinityService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkComponent {

    private val Client by lazy {
        val httpLoggingInterceptor = HttpLoggingInterceptor {
            Log.d("OkHttpClient", it)
        }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    val infinityService by lazy { InfinityService(Client) }
    val nodeResolver by lazy {
        NodeResolver.Builder()
            .client(Client)
            .build()
    }
}
