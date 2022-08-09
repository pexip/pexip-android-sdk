package com.pexip.sdk.registration

public sealed interface RegistrationEvent {

    public val at: Long
}

public data class IncomingRegistrationEvent(
    override val at: Long,
    val conferenceAlias: String,
    val remoteDisplayName: String,
    val token: String,
) : RegistrationEvent

public data class IncomingCancelledRegistrationEvent(
    override val at: Long,
    val token: String,
) : RegistrationEvent

public data class FailureRegistrationEvent(
    override val at: Long,
    val t: Throwable,
) : RegistrationEvent
