package com.pexip.sdk.conference

public sealed interface ConferenceEvent {

    public val at: Long
}

public data class PresentationStartConferenceEvent(override val at: Long) : ConferenceEvent

public data class PresentationStopConferenceEvent(override val at: Long) : ConferenceEvent
