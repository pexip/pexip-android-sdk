package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegisteredDevice

internal class RealRegisteredDevicesFetcher(
    private val step: InfinityService.RegistrationStep,
    private val store: TokenStore,
) : RegisteredDevicesFetcher {

    override fun fetch(query: String): List<RegisteredDevice> =
        step.registrations(store.get(), query)
            .execute()
            .map {
                RegisteredDevice(
                    alias = it.alias,
                    description = it.description,
                    username = it.username
                )
            }
}
