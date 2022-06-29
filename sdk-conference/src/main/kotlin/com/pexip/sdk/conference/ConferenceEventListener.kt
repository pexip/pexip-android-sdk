package com.pexip.sdk.conference

public fun interface ConferenceEventListener {

    /**
     * Invoked when a new [ConferenceEvent] is received.
     *
     * @param event a conference event
     */
    public fun onConferenceEvent(event: ConferenceEvent)
}
