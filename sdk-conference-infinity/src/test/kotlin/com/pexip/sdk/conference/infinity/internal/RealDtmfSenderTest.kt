package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.TokenStore
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RealDtmfSenderTest {

    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `dtmf returns`() {
        val digits = Random.nextDigits(8)
        val step = object : TestParticipantTest() {

            override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> {
                assertEquals(digits, request.digits)
                assertEquals(store.get().token, token)
                return object : TestCall<Boolean> {
                    override fun execute(): Boolean = Random.nextBoolean()
                }
            }
        }
        val sender = RealDtmfSender(store, step)
        sender.send(digits)
    }
}
