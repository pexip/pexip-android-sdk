package com.pexip.sdk.api

public fun interface EventSourceFactory {

    /**
     * Creates a new event source and immediately returns it. Creating an event source initiates an
     * asynchronous process to connect the socket. Once that succeeds or fails, `listener` will be
     * notified. The caller must cancel the returned event source when it is no longer in use.
     */
    public fun create(listener: EventSourceListener): EventSource
}
