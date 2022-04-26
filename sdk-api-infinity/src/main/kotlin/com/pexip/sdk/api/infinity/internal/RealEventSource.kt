package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.Event
import com.pexip.sdk.api.infinity.PresentationStopEvent
import kotlinx.serialization.json.Json
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

internal class RealEventSource(
    factory: okhttp3.sse.EventSource.Factory,
    request: okhttp3.Request,
    json: Json,
    listener: EventSourceListener,
) : EventSource {

    private val l = object : okhttp3.sse.EventSourceListener() {

        private val skipPresentationStop = AtomicBoolean(true)

        override fun onOpen(eventSource: okhttp3.sse.EventSource, response: Response) {
            listener.onOpen(this@RealEventSource)
        }

        override fun onEvent(
            eventSource: okhttp3.sse.EventSource,
            id: String?,
            type: String?,
            data: String,
        ) {
            val event = Event(json, id, type, data) ?: return
            if (event is PresentationStopEvent && skipPresentationStop.compareAndSet(true, false)) {
                return
            }
            listener.onEvent(this@RealEventSource, event)
        }

        override fun onFailure(
            eventSource: okhttp3.sse.EventSource,
            t: Throwable?,
            response: Response?,
        ) {
            listener.onClosed(this@RealEventSource, t)
        }

        override fun onClosed(eventSource: okhttp3.sse.EventSource) {
            listener.onClosed(this@RealEventSource, null)
        }
    }
    private val source = factory.newEventSource(request, l)

    override fun cancel() {
        source.cancel()
    }
}
