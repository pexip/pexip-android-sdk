package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.registration.IncomingCancelledRegistrationEvent
import com.pexip.sdk.registration.IncomingRegistrationEvent

internal inline fun RegistrationEvent(
    event: Event,
    at: () -> Long = System::currentTimeMillis,
) = when (event) {
    is IncomingEvent -> IncomingRegistrationEvent(
        at = at(),
        conferenceAlias = event.conferenceAlias,
        remoteDisplayName = event.remoteDisplayName,
        token = event.token
    )
    is IncomingCancelledEvent -> IncomingCancelledRegistrationEvent(
        at = at(),
        token = event.token
    )
    else -> null
}
