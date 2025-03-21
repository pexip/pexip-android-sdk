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
package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegisteredDevice

internal class RegisteredDevicesFetcher(
    private val step: InfinityService.RegistrationStep,
    private val store: TokenStore,
) {

    suspend fun fetch(query: String): List<RegisteredDevice> =
        step.registrations(store.token.value, query)
            .await()
            .map(::RegisteredDevice)

    @Suppress("ktlint:standard:function-naming")
    private fun RegisteredDevice(response: RegistrationResponse) = RegisteredDevice(
        alias = response.alias,
        description = response.description,
        username = response.username,
    )
}
