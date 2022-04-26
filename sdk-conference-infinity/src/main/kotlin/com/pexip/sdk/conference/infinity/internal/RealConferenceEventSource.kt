package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal class RealConferenceEventSource(
    private val store: TokenStore,
    private val conferenceStep: InfinityService.ConferenceStep,
    private val executor: ScheduledExecutorService,
) : ConferenceEventSource, EventSourceListener {

    private val attempts = AtomicLong()
    private val createEventSourceRunnable = Runnable {
        source = conferenceStep.events(store.get()).create(this)
    }
    private val cancelEventSourceRunnable = Runnable {
        source?.cancel()
    }
    private val listeners = CopyOnWriteArraySet<ConferenceEventListener>()

    @Volatile
    private var future = executor.submit(createEventSourceRunnable)

    @Volatile
    private var source: EventSource? = null

    override fun onOpen(eventSource: EventSource) {
        attempts.set(0)
    }

    override fun onEvent(eventSource: EventSource, event: Event) {
        val at = System.currentTimeMillis()
        val conferenceEvent = when (event) {
            is PresentationStartEvent -> PresentationStartConferenceEvent(
                at = at,
                presenterId = event.presenterId,
                presenterName = event.presenterName
            )
            is PresentationStopEvent -> PresentationStopConferenceEvent(at)
            is MessageReceivedEvent -> MessageReceivedConferenceEvent(
                at = at,
                participantId = event.participantId,
                participantName = event.participantName,
                type = event.type,
                payload = event.payload
            )
            else -> return
        }
        listeners.forEach { it.onConferenceEvent(conferenceEvent) }
    }

    override fun onClosed(eventSource: EventSource, t: Throwable?) {
        if (executor.isShutdown || t !is IOException) return
        val attempts = attempts.incrementAndGet()
        val delay = (attempts * 1000).coerceAtMost(5000)
        future = executor.schedule(createEventSourceRunnable, delay, TimeUnit.MILLISECONDS)
    }

    override fun registerConferenceEventListener(listener: ConferenceEventListener) {
        listeners += listener
    }

    override fun unregisterConferenceEventListener(listener: ConferenceEventListener) {
        listeners -= listener
    }

    override fun onConferenceEvent(event: ConferenceEvent) {
        listeners.forEach { it.onConferenceEvent(event) }
    }

    override fun cancel() {
        future.cancel(true)
        executor.submit(cancelEventSourceRunnable)
    }
}
