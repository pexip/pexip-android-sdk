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
package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegisteredDevice
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class RegisteredDevicesFetcherTest {

    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `fetch triggers onFailure`() = runTest {
        val throwable = Throwable()
        val q = Random.nextString(8)
        val step = object : TestRegistrationStep() {

            override fun registrations(
                token: String,
                query: String,
            ): Call<List<RegistrationResponse>> = object : TestCall<List<RegistrationResponse>> {

                override fun enqueue(callback: Callback<List<RegistrationResponse>>) {
                    assertEquals(store.get().token, token)
                    assertEquals(q, query)
                    callback.onFailure(this, throwable)
                }
            }
        }
        val fetcher = RegisteredDevicesFetcher(step, store)
        assertEquals(throwable, assertFails { fetcher.fetch(q) })
    }

    @Test
    fun `fetch triggers onSuccess`() = runTest {
        val responses = List(10) {
            val username = "user$it"
            RegistrationResponse(
                alias = "$username@example.com",
                description = "User #$it",
                username = username,
            )
        }
        val q = Random.nextString(8)
        val step = object : TestRegistrationStep() {

            override fun registrations(
                token: String,
                query: String,
            ): Call<List<RegistrationResponse>> = object : TestCall<List<RegistrationResponse>> {

                override fun enqueue(callback: Callback<List<RegistrationResponse>>) {
                    assertEquals(store.get().token, token)
                    assertEquals(q, query)
                    callback.onSuccess(this, responses)
                }
            }
        }
        val fetcher = RegisteredDevicesFetcher(step, store)
        assertEquals(
            expected = responses.map {
                RegisteredDevice(
                    alias = it.alias,
                    description = it.description,
                    username = it.username,
                )
            },
            actual = fetcher.fetch(q),
        )
    }
}