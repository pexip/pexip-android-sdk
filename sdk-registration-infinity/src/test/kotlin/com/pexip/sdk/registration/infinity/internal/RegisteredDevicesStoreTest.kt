package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.RegisteredDevice
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class RegisteredDevicesStoreTest {

    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `fetch triggers onFailure`() {
        val throwable = Throwable()
        val q = Random.nextString(8)
        val step = object : TestRegistrationStep() {

            override fun registrations(
                token: String,
                query: String,
            ): Call<List<RegistrationResponse>> = object : TestCall<List<RegistrationResponse>> {

                override fun execute(): List<RegistrationResponse> {
                    assertEquals(store.get().token, token)
                    assertEquals(q, query)
                    throw throwable
                }
            }
        }
        val fetcher = RealRegisteredDevicesFetcher(step, store)
        assertEquals(throwable, assertFails { fetcher.fetch(q) })
    }

    @Test
    fun `fetch triggers onSuccess`() {
        val responses = List(10) {
            val username = "user$it"
            RegistrationResponse(
                alias = "$username@example.com",
                description = "User #$it",
                username = username
            )
        }
        val q = Random.nextString(8)
        val step = object : TestRegistrationStep() {

            override fun registrations(
                token: String,
                query: String,
            ): Call<List<RegistrationResponse>> = object : TestCall<List<RegistrationResponse>> {

                override fun execute(): List<RegistrationResponse> {
                    assertEquals(store.get().token, token)
                    assertEquals(q, query)
                    return responses
                }
            }
        }
        val fetcher = RealRegisteredDevicesFetcher(step, store)
        fetcher.fetch(q)
        assertEquals(
            expected = responses.map {
                RegisteredDevice(
                    alias = it.alias,
                    description = it.description,
                    username = it.username
                )
            },
            actual = fetcher.fetch(q)
        )
    }
}
