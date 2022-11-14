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
import com.pexip.sdk.registration.infinity.internal.RegisteredDevicesFetcher
import com.pexip.sdk.registration.infinity.internal.RegistrationEvent
import com.pexip.sdk.registration.infinity.internal.RegistrationEventSource
import com.pexip.sdk.registration.infinity.internal.maybeSubmit
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public class InfinityRegistration private constructor(
    private val source: RegistrationEventSource,
    private val fetcher: RegisteredDevicesFetcher,
    private val refresher: TokenRefresher,
    private val executor: ScheduledExecutorService,
) : Registration {

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
            response = response
        )

        @JvmStatic
        public fun create(
            step: InfinityService.RegistrationStep,
            response: RequestRegistrationTokenResponse,
        ): InfinityRegistration {
            val store = TokenStore.create(response)
            val executor = Executors.newSingleThreadScheduledExecutor()
            val source = RealRegistrationEventSource(step, store, executor)
            val refresher = TokenRefresher.create(step, store, executor) {
                source.onRegistrationEvent(RegistrationEvent(it))
            }
            return InfinityRegistration(
                source = source,
                fetcher = RealRegisteredDevicesFetcher(step, store),
                refresher = refresher,
                executor = executor
            )
        }
    }
}
