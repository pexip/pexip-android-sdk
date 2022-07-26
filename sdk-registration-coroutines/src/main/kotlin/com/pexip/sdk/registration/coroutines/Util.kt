package com.pexip.sdk.registration.coroutines

import com.pexip.sdk.registration.Registration
import com.pexip.sdk.registration.RegistrationEvent
import com.pexip.sdk.registration.RegistrationEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Converts this [Registration] to a [Flow] that emits [RegistrationEvent]s.
 *
 * @return a flow of registration events
 */
public fun Registration.getRegistrationEvents(): Flow<RegistrationEvent> = callbackFlow {
    val listener = RegistrationEventListener(::trySend)
    registerRegistrationEventListener(listener)
    awaitClose { unregisterRegistrationEventListener(listener) }
}
