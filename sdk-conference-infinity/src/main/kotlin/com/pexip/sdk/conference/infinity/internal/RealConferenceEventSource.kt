/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.ConferenceEventListener
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

internal class RealConferenceEventSource(
    private val store: TokenStore,
    private val conferenceStep: InfinityService.ConferenceStep,
    private val executor: ScheduledExecutorService,
) : ConferenceEventSource, EventSourceListener {

    private val attempts = AtomicLong()
    private val skipPresentationStop = AtomicBoolean(true)
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
        skipPresentationStop.set(true)
    }

    override fun onEvent(eventSource: EventSource, event: Event) {
        if (event is PresentationStopEvent && skipPresentationStop.compareAndSet(true, false)) {
            return
        }
        val conferenceEvent = ConferenceEvent(event) ?: return
        onConferenceEvent(conferenceEvent)
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
