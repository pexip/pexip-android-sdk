package com.pexip.sdk.api

public interface EventSourceListener {

    /**
     * Invoked when an event source has been accepted by the remote peer and may begin transmitting
     * events.
     */
    public fun onOpen(eventSource: EventSource)

    /**
     * Invoked when a new [Event] has been received.
     */
    public fun onEvent(eventSource: EventSource, event: Event)

    /**
     * Invoked when an event source has been closed. Incoming events may have been lost.
     * No further calls to this listener will be made.
     */
    public fun onClosed(eventSource: EventSource, t: Throwable?)
}
