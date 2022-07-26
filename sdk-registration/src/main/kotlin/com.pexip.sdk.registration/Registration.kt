package com.pexip.sdk.registration

public interface Registration {

    public fun registerRegistrationEventListener(listener: RegistrationEventListener)

    public fun unregisterRegistrationEventListener(listener: RegistrationEventListener)

    public fun dispose()
}
