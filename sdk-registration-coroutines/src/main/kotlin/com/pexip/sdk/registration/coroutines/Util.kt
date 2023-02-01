/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
