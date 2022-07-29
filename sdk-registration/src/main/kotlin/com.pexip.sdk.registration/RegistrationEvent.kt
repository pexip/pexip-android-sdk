package com.pexip.sdk.registration

public sealed interface RegistrationEvent

public data class IncomingRegistrationEvent(
    val conferenceAlias: String,
    val remoteDisplayName: String,
    val token: String,
) : RegistrationEvent

public data class IncomingCancelledRegistrationEvent(val token: String) : RegistrationEvent
