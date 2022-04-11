package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.EventSourceListener
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

internal class RealEventSourceFactory(
    client: OkHttpClient,
    private val request: Request,
    private val json: Json,
) : EventSourceFactory {

    private val factory = EventSources.createFactory(client) { readTimeout(0, TimeUnit.SECONDS) }

    override fun create(listener: EventSourceListener): EventSource =
        RealEventSource(factory, request, json, listener)
}
