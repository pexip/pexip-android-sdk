/*
 * Copyright 2022-2024 Pexip AS
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

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.TokenStore.Companion.refreshTokenIn
import com.pexip.sdk.core.retry
import com.pexip.sdk.infinity.UnsupportedInfinityException
import com.pexip.sdk.registration.RegisteredDevicesCallback
import com.pexip.sdk.registration.Registration
import com.pexip.sdk.registration.RegistrationEvent
import com.pexip.sdk.registration.RegistrationEventListener
import com.pexip.sdk.registration.infinity.internal.RegisteredDevicesFetcher
import com.pexip.sdk.registration.infinity.internal.RegistrationEvent
import com.pexip.sdk.registration.infinity.internal.registrationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

public class InfinityRegistration private constructor(
    step: InfinityService.RegistrationStep,
    response: RequestRegistrationTokenResponse,
) : Registration {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val scope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
    private val store = TokenStore.create(response)
    private val fetcher = RegisteredDevicesFetcher(step, store)
    private val registrationEvent = step
        .registrationEvent(store)
        .shareIn(scope, SharingStarted.Lazily)
    private val listeners = CopyOnWriteArraySet<RegistrationEventListener>()
    private val mutableRegistrationEvent = MutableSharedFlow<RegistrationEvent>()

    override val directoryEnabled: Boolean = response.directoryEnabled

    override val routeViaRegistrar: Boolean = response.routeViaRegistrar

    init {
        store.refreshTokenIn(
            scope = scope,
            refreshToken = { retry { step.refreshToken(it).await() } },
            releaseToken = { retry { step.releaseToken(it).await() } },
            onFailure = { mutableRegistrationEvent.emit(RegistrationEvent(it)) },
        )
        merge(registrationEvent, mutableRegistrationEvent)
            .onEach { event -> listeners.forEach { it.onRegistrationEvent(event) } }
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope)
    }

    override fun getRegisteredDevices(query: String, callback: RegisteredDevicesCallback) {
        scope.launch {
            try {
                callback.onSuccess(fetcher.fetch(query))
            } catch (t: Throwable) {
                callback.onFailure(t)
            }
        }
    }

    override fun registerRegistrationEventListener(listener: RegistrationEventListener) {
        listeners += listener
    }

    override fun unregisterRegistrationEventListener(listener: RegistrationEventListener) {
        listeners -= listener
    }

    override fun dispose() {
        scope.cancel()
        executor.shutdown()
        listeners.clear()
    }

    public companion object {

        /**
         * Creates a new instance of [InfinityRegistration].
         *
         * @param service an instance of [InfinityService]
         * @param node a URL of the node
         * @param deviceAlias a device alias
         * @param response a request registration token response
         * @return an instance of [InfinityRegistration]
         * @throws UnsupportedInfinityException if the version of Infinity is not supported
         */
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

        /**
         * Creates a new instance of [InfinityRegistration].
         *
         * @param step a registration step
         * @param response a request registration token response
         * @return an instance of [InfinityRegistration]
         * @throws UnsupportedInfinityException if the version of Infinity is not supported
         */
        @JvmStatic
        public fun create(
            step: InfinityService.RegistrationStep,
            response: RequestRegistrationTokenResponse,
        ): InfinityRegistration {
            if (response.version.versionId < "29") {
                throw UnsupportedInfinityException(response.version.versionId)
            }
            return InfinityRegistration(step, response)
        }
    }
}
