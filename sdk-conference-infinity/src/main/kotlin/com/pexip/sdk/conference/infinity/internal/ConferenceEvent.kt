package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent

internal inline fun ConferenceEvent(
    event: Event,
    at: () -> Long = System::currentTimeMillis,
) = when (event) {
    is PresentationStartEvent -> PresentationStartConferenceEvent(
        at = at(),
        presenterId = event.presenterId,
        presenterName = event.presenterName
    )
    is PresentationStopEvent -> PresentationStopConferenceEvent(at())
    is MessageReceivedEvent -> MessageReceivedConferenceEvent(
        at = at(),
        participantId = event.participantId,
        participantName = event.participantName,
        type = event.type,
        payload = event.payload
    )
    is DisconnectEvent -> DisconnectConferenceEvent(
        at = at(),
        reason = event.reason
    )
    else -> null
}

internal inline fun ConferenceEvent(t: Throwable, at: () -> Long = System::currentTimeMillis) =
    FailureConferenceEvent(at(), t)
