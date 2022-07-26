package com.pexip.sdk.registration

public fun interface RegistrationEventListener {

    /**
     * Invoked when a new [RegistrationEvent] is received.
     *
     * @param event a registration event
     */
    public fun onRegistrationEvent(event: RegistrationEvent)
}
