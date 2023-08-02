/*
 * Copyright 2022-2023 Pexip AS
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
package com.pexip.sdk.registration.infinity

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import com.pexip.sdk.api.infinity.TokenRefresher
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegisteredDevicesCallback
import com.pexip.sdk.registration.Registration
import com.pexip.sdk.registration.RegistrationEventListener
import com.pexip.sdk.registration.infinity.internal.RealRegisteredDevicesFetcher
import com.pexip.sdk.registration.infinity.internal.RealRegistrationEventSource
import com.pexip.sdk.registration.infinity.internal.RegistrationEvent
import com.pexip.sdk.registration.infinity.internal.maybeSubmit
import java.net.URL
import java.util.concurrent.Executors
import java.util.logging.Logger

public class InfinityRegistration private constructor(
    step: InfinityService.RegistrationStep,
    response: RequestRegistrationTokenResponse,
) : Registration {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val store = TokenStore.create(response)
    private val source = RealRegistrationEventSource(step, store, executor)
    private val refresher = TokenRefresher.create(step, store, executor) {
        source.onRegistrationEvent(RegistrationEvent(it))
    }
    private val fetcher = RealRegisteredDevicesFetcher(step, store)

    override val directoryEnabled: Boolean = response.directoryEnabled

    override val routeViaRegistrar: Boolean = response.routeViaRegistrar

    override fun getRegisteredDevices(query: String, callback: RegisteredDevicesCallback) {
        executor.maybeSubmit {
            try {
                callback.onSuccess(fetcher.fetch(query))
            } catch (t: Throwable) {
                callback.onFailure(t)
            }
        }
    }

    override fun registerRegistrationEventListener(listener: RegistrationEventListener) {
        source.registerRegistrationEventListener(listener)
    }

    override fun unregisterRegistrationEventListener(listener: RegistrationEventListener) {
        source.unregisterRegistrationEventListener(listener)
    }

    override fun dispose() {
        source.cancel()
        refresher.cancel()
        executor.shutdown()
    }

    public companion object {

        @JvmStatic
        public fun create(
            service: InfinityService,
            node: URL,
            deviceAlias: String,
            response: RequestRegistrationTokenResponse,
        ): InfinityRegistration = create(
            step = service.newRequest(node).registration(deviceAlias),
            response = response,
        )

        @JvmStatic
        public fun create(
            step: InfinityService.RegistrationStep,
            response: RequestRegistrationTokenResponse,
        ): InfinityRegistration {
            if (response.version.versionId < "29") {
                val logger = Logger.getLogger("InfinityRegistration")
                val msg = buildString {
                    append("Infinity ")
                    append(response.version.versionId)
                    append(" is not officially supported by the SDK.")
                    append(" Please upgrade your Infinity deployment to 29 or newer.")
                }
                logger.warning(msg)
            }
            return InfinityRegistration(step, response)
        }
    }
}
