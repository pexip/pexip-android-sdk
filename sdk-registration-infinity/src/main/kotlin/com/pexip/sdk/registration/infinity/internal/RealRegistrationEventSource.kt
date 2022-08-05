package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegistrationEvent
import com.pexip.sdk.registration.RegistrationEventListener
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal class RealRegistrationEventSource(
    private val step: InfinityService.RegistrationStep,
    private val store: TokenStore,
    private val executor: ScheduledExecutorService,
) : RegistrationEventSource, EventSourceListener {

    private val attempts = AtomicLong()
    private val createEventSourceRunnable = Runnable {
        source = step.events(store.get()).create(this)
    }
    private val cancelEventSourceRunnable = Runnable {
        source?.cancel()
    }
    private val listeners = CopyOnWriteArraySet<RegistrationEventListener>()

    @Volatile
    private var future = executor.submit(createEventSourceRunnable)

    @Volatile
    private var source: EventSource? = null

    override fun onOpen(eventSource: EventSource) {
        attempts.set(0)
    }

    override fun onEvent(eventSource: EventSource, event: Event) {
        val registrationEvent = RegistrationEvent(event) ?: return
        listeners.forEach { it.onRegistrationEvent(registrationEvent) }
    }

    override fun onClosed(eventSource: EventSource, t: Throwable?) {
        if (executor.isShutdown || t !is IOException) return
        val attempts = attempts.incrementAndGet()
        val delay = (attempts * 1000).coerceAtMost(5000)
        future = executor.schedule(createEventSourceRunnable, delay, TimeUnit.MILLISECONDS)
    }

    override fun registerRegistrationEventListener(listener: RegistrationEventListener) {
        listeners += listener
    }

    override fun unregisterRegistrationEventListener(listener: RegistrationEventListener) {
        listeners -= listener
    }

    override fun onRegistrationEvent(event: RegistrationEvent) {
        listeners.forEach { it.onRegistrationEvent(event) }
    }

    override fun cancel() {
        future.cancel(true)
        executor.submit(cancelEventSourceRunnable)
    }
}
