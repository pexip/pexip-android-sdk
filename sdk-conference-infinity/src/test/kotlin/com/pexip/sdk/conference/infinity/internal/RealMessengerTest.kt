package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class RealMessengerTest {

    private lateinit var participantId: UUID
    private lateinit var participantName: String
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        participantId = UUID.randomUUID()
        participantName = Random.nextString(8)
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `message returns true`() {
        var conferenceEvent: ConferenceEvent? = null
        val type = "text/plain"
        val payload = Random.nextString(8)
        val at = Random.nextLong(Long.MAX_VALUE)
        val messenger = RealMessenger(
            participantId = participantId,
            participantName = participantName,
            store = store,
            conferenceStep = object : TestConferenceStep() {

                override fun message(request: MessageRequest, token: String): Call<Boolean> =
                    object : TestCall<Boolean> {

                        override fun execute(): Boolean {
                            assertEquals(type, request.type)
                            assertEquals(payload, request.payload)
                            assertEquals(store.get().token, token)
                            return true
                        }
                    }
            },
            listener = { conferenceEvent = it },
            atProvider = { at }
        )
        messenger.message(payload)
        assertEquals(
            expected = MessageReceivedConferenceEvent(
                at = at,
                participantId = participantId,
                participantName = participantName,
                type = type,
                payload = payload
            ),
            actual = conferenceEvent
        )
    }

    @Test
    fun `message returns false`() {
        val type = "text/plain"
        val payload = Random.nextString(8)
        val at = Random.nextLong(Long.MAX_VALUE)
        val messenger = RealMessenger(
            participantId = participantId,
            participantName = participantName,
            store = store,
            conferenceStep = object : TestConferenceStep() {

                override fun message(request: MessageRequest, token: String): Call<Boolean> =
                    object : TestCall<Boolean> {

                        override fun execute(): Boolean {
                            assertEquals(type, request.type)
                            assertEquals(payload, request.payload)
                            assertEquals(store.get().token, token)
                            return false
                        }
                    }
            },
            listener = { fail() },
            atProvider = { at }
        )
        messenger.message(payload)
    }

    @Test
    fun `message throws`() {
        val type = "text/plain"
        val payload = Random.nextString(8)
        val at = Random.nextLong(Long.MAX_VALUE)
        val t = Throwable()
        val messenger = RealMessenger(
            participantId = participantId,
            participantName = participantName,
            store = store,
            conferenceStep = object : TestConferenceStep() {

                override fun message(request: MessageRequest, token: String): Call<Boolean> =
                    object : TestCall<Boolean> {

                        override fun execute(): Boolean {
                            assertEquals(type, request.type)
                            assertEquals(payload, request.payload)
                            assertEquals(store.get().token, token)
                            throw t
                        }
                    }
            },
            listener = { fail() },
            atProvider = { at }
        )
        messenger.message(payload)
    }
}
