package com.pexip.sdk.registration.coroutines

import com.pexip.sdk.registration.RegisteredDevice
import com.pexip.sdk.registration.RegisteredDevicesCallback
import com.pexip.sdk.registration.Registration
import com.pexip.sdk.registration.RegistrationEvent
import com.pexip.sdk.registration.RegistrationEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

/**
 * Suspends until a new list of registered devices is available or until an error is encountered.
 *
 * @param query a search query
 * @return a list of registered devices
 */
public suspend fun Registration.getRegisteredDevices(query: String = ""): List<RegisteredDevice> =
    suspendCoroutine {
        val callback = object : RegisteredDevicesCallback {

            override fun onSuccess(devices: List<RegisteredDevice>) {
                it.resume(devices)
            }

            override fun onFailure(t: Throwable) {
                it.resumeWithException(t)
            }
        }
        getRegisteredDevices(query, callback)
    }
