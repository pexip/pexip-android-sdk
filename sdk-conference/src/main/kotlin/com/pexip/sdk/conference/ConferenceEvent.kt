package com.pexip.sdk.conference

import java.util.UUID

public sealed interface ConferenceEvent {

    public val at: Long
}

public data class PresentationStartConferenceEvent(
    override val at: Long,
    val presenterId: UUID,
    val presenterName: String,
) : ConferenceEvent

public data class PresentationStopConferenceEvent(override val at: Long) : ConferenceEvent

public data class MessageReceivedConferenceEvent(
    override val at: Long,
    val participantId: UUID,
    val participantName: String,
    val type: String,
    val payload: String,
) : ConferenceEvent
