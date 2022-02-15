package com.pexip.sdk.video.internal

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

internal val OkHttpClient by lazy { OkHttpClient() }
internal val Json by lazy { Json { ignoreUnknownKeys = true } }
