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
package com.pexip.sdk.conference.infinity.internal

import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.support.fail
import assertk.tableOf
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.AckRequest
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateEvent
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.NewOfferEvent
import com.pexip.sdk.api.infinity.PeerDisconnectEvent
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse
import com.pexip.sdk.api.infinity.UpdateSdpEvent
import com.pexip.sdk.media.CandidateSignalingEvent
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.OfferSignalingEvent
import com.pexip.sdk.media.RestartSignalingEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RealMediaConnectionSignalingTest {

    private lateinit var store: TokenStore
    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var iceServers: List<IceServer>

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
        event = MutableSharedFlow(extraBufferCapacity = 1)
        iceServers = List(10) {
            IceServer.Builder(listOf("turn:turn$it.example.com:3478?transport=udp"))
                .username("${it shl 1}")
                .password("${it shr 1}")
                .build()
        }
    }

    @Test
    fun `iceServers return IceServer list`() {
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        assertThat(signaling::iceServers).isEqualTo(iceServers)
    }

    @Test
    fun `iceTransportsRelayOnly returns correct value`() {
        tableOf("iceTransportsRelayOnly")
            .row(false)
            .row(true)
            .forAll {
                val signaling = RealMediaConnectionSignaling(
                    store = store,
                    event = event,
                    participantStep = object : TestParticipantStep() {},
                    iceServers = iceServers,
                    iceTransportsRelayOnly = it,
                    dataChannelId = Random.nextDataChannelId(),
                )
                assertThat(signaling::iceTransportsRelayOnly).isEqualTo(it)
            }
    }

    @Test
    fun `dataChannelId returns correct value`() {
        val dataChannelId = Random.nextDataChannelId()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = dataChannelId,
        )
        assertThat(signaling::dataChannelId).isEqualTo(dataChannelId)
    }

    @Test
    fun `NewOfferEvent is mapped to OfferSignalingEvent`() = runTest {
        val participantStep = object : TestParticipantStep() {}
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = participantStep,
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.event.test {
            repeat(10) {
                val e = NewOfferEvent(Random.nextString(8))
                event.emit(e)
                assertThat(awaitItem(), "event")
                    .isInstanceOf<OfferSignalingEvent>()
                    .prop(OfferSignalingEvent::description)
                    .isEqualTo(e.sdp)
            }
        }
    }

    @Test
    fun `UpdateSdpEvent is mapped to OfferSignalingEvent`() = runTest {
        val participantStep = object : TestParticipantStep() {}
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = participantStep,
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.event.test {
            repeat(10) {
                val e = UpdateSdpEvent(Random.nextString(8))
                event.emit(e)
                assertThat(awaitItem(), "event")
                    .isInstanceOf<OfferSignalingEvent>()
                    .prop(OfferSignalingEvent::description)
                    .isEqualTo(e.sdp)
            }
        }
    }

    @Test
    fun `NewCandidateEvent is mapped to CandidateSignalingEvent`() = runTest {
        val participantStep = object : TestParticipantStep() {}
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = participantStep,
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.event.test {
            repeat(10) {
                val e = NewCandidateEvent(
                    candidate = Random.nextString(8),
                    mid = Random.nextString(8),
                    ufrag = Random.nextString(8),
                    pwd = Random.nextString(8),
                )
                event.emit(e)
                assertThat(awaitItem(), "event")
                    .isInstanceOf<CandidateSignalingEvent>()
                    .all {
                        prop(CandidateSignalingEvent::mid).isEqualTo(e.mid)
                        prop(CandidateSignalingEvent::candidate).isEqualTo(e.candidate)
                    }
            }
        }
    }

    @Test
    fun `PeerDisconnectedEvent is mapped to RestartSignalingEvent`() = runTest {
        val participantStep = object : TestParticipantStep() {}
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = participantStep,
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.event.test {
            repeat(10) {
                event.emit(PeerDisconnectEvent)
                assertThat(awaitItem(), "event").isInstanceOf<RestartSignalingEvent>()
            }
        }
    }

    @Test
    fun `ignores unknown Events`() = runTest {
        val participantStep = object : TestParticipantStep() {}
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = participantStep,
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.event.test {
            repeat(10) { event.emit(object : Event {}) }
            expectNoEvents()
        }
    }

    @Test
    fun `onOffer() returns Answer (first call)`() = runTest {
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMain = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val responses = setOf(
            CallsResponse(
                callId = UUID.randomUUID(),
                sdp = Random.nextString(8),
            ),
            CallsResponse(
                callId = UUID.randomUUID(),
                offerIgnored = true,
            ),
            // Malformed responses should still be handled correctly
            CallsResponse(callId = UUID.randomUUID()),
            CallsResponse(
                callId = UUID.randomUUID(),
                sdp = Random.nextString(8),
                offerIgnored = true,
            ),
        )
        responses.forEach { response ->
            val callStep = object : TestCallStep() {}
            val participantStep = object : TestParticipantStep() {
                override fun calls(request: CallsRequest, token: String): Call<CallsResponse> =
                    object : TestCall<CallsResponse> {
                        override fun enqueue(callback: Callback<CallsResponse>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(request::present).isEqualTo(if (presentationInMain) "main" else null)
                            assertThat(request::callType).isEqualTo(callType)
                            assertThat(store).contains(token)
                            callback.onSuccess(this, response)
                        }
                    }

                override fun call(callId: UUID): InfinityService.CallStep {
                    assertThat(callId, "callId").isEqualTo(response.callId)
                    return callStep
                }
            }
            val signaling = RealMediaConnectionSignaling(
                store = store,
                event = event,
                participantStep = participantStep,
                iceServers = iceServers,
                iceTransportsRelayOnly = Random.nextBoolean(),
                dataChannelId = Random.nextDataChannelId(),
            )
            val answer = signaling.onOffer(
                callType = callType,
                description = sdp,
                presentationInMain = presentationInMain,
                fecc = fecc,
            )
            val expectedAnswer = when {
                response.offerIgnored -> null
                response.sdp.isBlank() -> null
                else -> response.sdp
            }
            assertThat(answer, "answer").isEqualTo(expectedAnswer)
            assertThat(signaling.callStep.await(), "callStep").isEqualTo(callStep)
        }
    }

    @Test
    fun `onOffer() returns Answer (subsequent calls)`() = runTest {
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMain = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val responses = setOf(
            UpdateResponse(sdp = Random.nextString(8)),
            UpdateResponse(offerIgnored = true),
            UpdateResponse(),
            UpdateResponse(sdp = Random.nextString(8), offerIgnored = true),
        )
        responses.forEach { response ->
            val signaling = RealMediaConnectionSignaling(
                store = store,
                event = event,
                participantStep = object : TestParticipantStep() {},
                iceServers = iceServers,
                callStep = object : TestCallStep() {
                    override fun update(
                        request: UpdateRequest,
                        token: String,
                    ): Call<UpdateResponse> = object : TestCall<UpdateResponse> {
                        override fun enqueue(callback: Callback<UpdateResponse>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(store).contains(token)
                            callback.onSuccess(this, response)
                        }
                    }
                },
                iceTransportsRelayOnly = Random.nextBoolean(),
                dataChannelId = Random.nextDataChannelId(),
            )
            val answer = signaling.onOffer(
                callType = callType,
                description = sdp,
                presentationInMain = presentationInMain,
                fecc = fecc,
            )
            val expectedAnswer = when {
                response.offerIgnored -> null
                response.sdp.isBlank() -> null
                else -> response.sdp
            }
            assertThat(answer, "answer").isEqualTo(expectedAnswer)
        }
    }

    @Test
    fun `onOfferIgnored() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun ack(request: AckRequest, token: String): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::sdp).isEmpty()
                            assertThat(request::offerIgnored).isTrue()
                            assertThat(store).contains(token)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onOfferIgnored()
        called.join()
    }

    @Test
    fun `onAnswer() returns`() = runTest {
        val called = Job()
        val sdp = Random.nextString(8)
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun ack(request: AckRequest, token: String): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(request::offerIgnored).isFalse()
                            assertThat(store).contains(token)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onAnswer(sdp)
        called.join()
    }

    @Test
    fun `onAck() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun ack(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onAck()
        called.join()
    }

    @Test
    fun `onCandidate() returns`() = runTest {
        val called = Job()
        val candidate = Random.nextString(8)
        val mid = Random.nextString(8)
        val ufrag = Random.nextString(8)
        val pwd = Random.nextString(8)
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::candidate).isEqualTo(candidate)
                            assertThat(request::mid).isEqualTo(mid)
                            assertThat(request::ufrag).isEqualTo(ufrag)
                            assertThat(request::pwd).isEqualTo(pwd)
                            assertThat(store).contains(token)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onCandidate(candidate, mid, ufrag, pwd)
        called.join()
    }

    @Test
    fun `onDtmf() returns`() = runTest {
        val called = Job()
        val digits = Random.nextDigits(8)
        val result = Random.nextBoolean()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> =
                    object : TestCall<Boolean> {
                        override fun enqueue(callback: Callback<Boolean>) {
                            assertThat(request::digits).isEqualTo(digits)
                            called.complete()
                            callback.onSuccess(this, result)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onDtmf(digits)
        called.join()
    }

    @Test
    fun `onAudioMuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun mute(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onAudioMuted()
        called.join()
    }

    @Test
    fun `onAudioUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun unmute(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onAudioUnmuted()
        called.join()
    }

    @Test
    fun `onVideoMuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun videoMuted(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onVideoMuted()
        called.join()
    }

    @Test
    fun `onVideoUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun videoUnmuted(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onVideoUnmuted()
        called.join()
    }

    @Test
    fun `onTakeFloor() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun takeFloor(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onTakeFloor()
        called.join()
    }

    @Test
    fun `onReleaseFloor() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            event = event,
            participantStep = object : TestParticipantStep() {
                override fun releaseFloor(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(store).contains(token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannelId = Random.nextDataChannelId(),
        )
        signaling.onReleaseFloor()
        called.join()
    }

    @Suppress("SameParameterValue")
    private fun read(fileName: String) = FileSystem.RESOURCES.read(fileName.toPath()) { readUtf8() }

    private fun Assert<TokenStore>.contains(token: String) = given {
        val actual = store.get().token
        if (actual == token) return
        fail(token, actual)
    }

    private fun Random.nextDataChannelId() = nextInt(-1, 65536)
}
