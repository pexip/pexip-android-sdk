package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.registration.RegistrationEventListener

internal interface RegistrationEventSource : RegistrationEventListener {

    fun registerRegistrationEventListener(listener: RegistrationEventListener)

    fun unregisterRegistrationEventListener(listener: RegistrationEventListener)

    fun cancel()
}
