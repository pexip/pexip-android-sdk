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
package com.pexip.sdk.conference.infinity.internal

import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
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
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse
import com.pexip.sdk.api.infinity.UpdateSdpEvent
import com.pexip.sdk.core.awaitSubscriptionCountAtLeast
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.Infinity
import com.pexip.sdk.infinity.test.nextCallId
import com.pexip.sdk.infinity.test.nextString
import com.pexip.sdk.media.CandidateSignalingEvent
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.OfferSignalingEvent
import com.pexip.sdk.media.RestartSignalingEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class MediaConnectionSignalingImplTest {

    private lateinit var store: TokenStore
    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var versionId: String
    private lateinit var iceServers: List<IceServer>

    @BeforeTest
    fun setUp() {
        store = TokenStore(Random.nextToken())
        event = MutableSharedFlow(extraBufferCapacity = 1)
        versionId = Infinity.VERSION_35
        iceServers = List(10) {
            IceServer.Builder(listOf("turn:turn$it.example.com:347?transport=udp"))
                .username("${it shl 1}")
                .password("${it shr 1}")
                .build()
        }
    }

    @Test
    fun `directMedia returns correct value`() = runTest {
        tableOf("directMedia")
            .row(false)
            .row(true)
            .forAll {
                val signaling = MediaConnectionSignalingImpl(
                    scope = backgroundScope,
                    event = event,
                    store = store,
                    participantStep = object : InfinityService.ParticipantStep {},
                    versionId = versionId,
                    directMedia = it,
                    iceServers = iceServers,
                    iceTransportsRelayOnly = Random.nextBoolean(),
                    dataChannel = null,
                )
                assertThat(signaling::directMedia).isEqualTo(it)
            }
    }

    @Test
    fun `iceServers return IceServer list`() = runTest {
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        assertThat(signaling::iceServers).isEqualTo(iceServers)
    }

    @Test
    fun `iceTransportsRelayOnly returns correct value`() = runTest {
        tableOf("iceTransportsRelayOnly")
            .row(false)
            .row(true)
            .forAll {
                val signaling = MediaConnectionSignalingImpl(
                    scope = backgroundScope,
                    event = event,
                    store = store,
                    participantStep = object : InfinityService.ParticipantStep {},
                    versionId = versionId,
                    directMedia = Random.nextBoolean(),
                    iceServers = iceServers,
                    iceTransportsRelayOnly = it,
                    dataChannel = null,
                )
                assertThat(signaling::iceTransportsRelayOnly).isEqualTo(it)
            }
    }

    @Test
    fun `dataChannel returns the correct value`() = runTest {
        val dataChannel = DataChannelImpl(Random.nextInt())
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = dataChannel,
        )
        assertThat(signaling::dataChannel).isEqualTo(dataChannel)
    }

    @Test
    fun `NewOfferEvent is mapped to OfferSignalingEvent`() = runTest {
        val participantStep = object : InfinityService.ParticipantStep {}
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = participantStep,
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.event.test {
            event.awaitSubscriptionCountAtLeast(1)
            repeat(10) {
                val e = NewOfferEvent(Random.nextString())
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
        val participantStep = object : InfinityService.ParticipantStep {}
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = participantStep,
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.event.test {
            event.awaitSubscriptionCountAtLeast(1)
            repeat(10) {
                val e = UpdateSdpEvent(Random.nextString())
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
        val participantStep = object : InfinityService.ParticipantStep {}
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = participantStep,
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.event.test {
            event.awaitSubscriptionCountAtLeast(1)
            repeat(10) {
                val e = NewCandidateEvent(
                    candidate = Random.nextString(),
                    mid = Random.nextString(),
                    ufrag = Random.nextString(),
                    pwd = Random.nextString(),
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
        val participantStep = object : InfinityService.ParticipantStep {}
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = participantStep,
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.event.test {
            event.awaitSubscriptionCountAtLeast(1)
            repeat(10) {
                event.emit(PeerDisconnectEvent)
                assertThat(awaitItem(), "event").isInstanceOf<RestartSignalingEvent>()
            }
        }
    }

    @Test
    fun `ignores unknown Events`() = runTest {
        val participantStep = object : InfinityService.ParticipantStep {}
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = participantStep,
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.event.test {
            event.awaitSubscriptionCountAtLeast(1)
            repeat(10) { event.emit(object : Event {}) }
            expectNoEvents()
        }
    }

    @Test
    fun `onOffer() returns Answer (first call)`() = runTest {
        val callType = Random.nextString()
        val sdp = read("session_description_original")
        val presentationInMain = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val responses = setOf(
            CallsResponse(
                callId = Random.nextCallId(),
                sdp = Random.nextString(),
            ),
            CallsResponse(
                callId = Random.nextCallId(),
                offerIgnored = true,
            ),
            // Malformed responses should still be handled correctly
            CallsResponse(callId = Random.nextCallId()),
            CallsResponse(
                callId = Random.nextCallId(),
                sdp = Random.nextString(),
                offerIgnored = true,
            ),
        )
        responses.forEach { response ->
            val callStep = object : InfinityService.CallStep {}
            val participantStep = object : InfinityService.ParticipantStep {
                override fun calls(request: CallsRequest, token: Token): Call<CallsResponse> =
                    object : TestCall<CallsResponse> {
                        override fun enqueue(callback: Callback<CallsResponse>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(request::present).isEqualTo(if (presentationInMain) "main" else null)
                            assertThat(request::callType).isEqualTo(callType)
                            assertThat(token).isEqualTo(store.token.value)
                            callback.onSuccess(this, response)
                        }
                    }

                override fun call(callId: CallId): InfinityService.CallStep {
                    assertThat(callId, "callId").isEqualTo(response.callId)
                    return callStep
                }
            }
            val signaling = MediaConnectionSignalingImpl(
                scope = backgroundScope,
                event = event,
                store = store,
                participantStep = participantStep,
                versionId = versionId,
                directMedia = Random.nextBoolean(),
                iceServers = iceServers,
                iceTransportsRelayOnly = Random.nextBoolean(),
                dataChannel = null,
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
        val callType = Random.nextString()
        val sdp = read("session_description_original")
        val presentationInMain = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val responses = setOf(
            UpdateResponse(sdp = Random.nextString()),
            UpdateResponse(offerIgnored = true),
            UpdateResponse(),
            UpdateResponse(sdp = Random.nextString(), offerIgnored = true),
        )
        responses.forEach { response ->
            val signaling = MediaConnectionSignalingImpl(
                scope = backgroundScope,
                event = event,
                store = store,
                participantStep = object : InfinityService.ParticipantStep {},
                versionId = versionId,
                directMedia = Random.nextBoolean(),
                iceServers = iceServers,
                callStep = object : InfinityService.CallStep {
                    override fun update(
                        request: UpdateRequest,
                        token: Token,
                    ): Call<UpdateResponse> = object : TestCall<UpdateResponse> {
                        override fun enqueue(callback: Callback<UpdateResponse>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(token).isEqualTo(store.token.value)
                            callback.onSuccess(this, response)
                        }
                    }
                },
                iceTransportsRelayOnly = Random.nextBoolean(),
                dataChannel = null,
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
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            callStep = object : InfinityService.CallStep {
                override fun ack(request: AckRequest, token: Token): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::sdp).isEmpty()
                            assertThat(request::offerIgnored).isTrue()
                            assertThat(token).isEqualTo(store.token.value)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onOfferIgnored()
        called.join()
    }

    @Test
    fun `onAnswer() returns`() = runTest {
        val called = Job()
        val sdp = Random.nextString()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            callStep = object : InfinityService.CallStep {
                override fun ack(request: AckRequest, token: Token): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::sdp).isEqualTo(sdp)
                            assertThat(request::offerIgnored).isFalse()
                            assertThat(token).isEqualTo(store.token.value)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAnswer(sdp)
        called.join()
    }

    @Test
    fun `onAck() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            callStep = object : InfinityService.CallStep {
                override fun ack(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAck()
        called.join()
    }

    @Test
    fun `onCandidate() returns`() = runTest {
        val called = Job()
        val candidate = Random.nextString()
        val mid = Random.nextString()
        val ufrag = Random.nextString()
        val pwd = Random.nextString()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            callStep = object : InfinityService.CallStep {
                override fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertThat(request::candidate).isEqualTo(candidate)
                            assertThat(request::mid).isEqualTo(mid)
                            assertThat(request::ufrag).isEqualTo(ufrag)
                            assertThat(request::pwd).isEqualTo(pwd)
                            assertThat(token).isEqualTo(store.token.value)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onCandidate(candidate, mid, ufrag, pwd)
        called.join()
    }

    @Test
    fun `onDtmf() returns`() = runTest {
        val called = Job()
        val digits = Random.nextDigits()
        val result = Random.nextBoolean()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {},
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            callStep = object : InfinityService.CallStep {
                override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
                    object : TestCall<Boolean> {
                        override fun enqueue(callback: Callback<Boolean>) {
                            assertThat(request::digits).isEqualTo(digits)
                            called.complete()
                            callback.onSuccess(this, result)
                        }
                    }
            },
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onDtmf(digits)
        called.join()
    }

    @Test
    fun `onAudioMuted() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun mute(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAudioMuted()
        called.join()
    }

    @Test
    fun `onAudioMuted() returns on v36+`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun clientMute(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = Infinity.VERSION_36,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAudioMuted()
        called.join()
    }

    @Test
    fun `onAudioUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun unmute(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAudioUnmuted()
        called.join()
    }

    @Test
    fun `onAudioUnmuted() returns on v36+`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun clientUnmute(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = Infinity.VERSION_36,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onAudioUnmuted()
        called.join()
    }

    @Test
    fun `onVideoMuted() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun videoMuted(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onVideoMuted()
        called.join()
    }

    @Test
    fun `onVideoUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun videoUnmuted(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onVideoUnmuted()
        called.join()
    }

    @Test
    fun `onTakeFloor() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun takeFloor(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onTakeFloor()
        called.join()
    }

    @Test
    fun `onReleaseFloor() returns`() = runTest {
        val called = Job()
        val signaling = MediaConnectionSignalingImpl(
            scope = backgroundScope,
            event = event,
            store = store,
            participantStep = object : InfinityService.ParticipantStep {
                override fun releaseFloor(token: Token): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertThat(token).isEqualTo(store.token.value)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            versionId = versionId,
            directMedia = Random.nextBoolean(),
            iceServers = iceServers,
            iceTransportsRelayOnly = Random.nextBoolean(),
            dataChannel = null,
        )
        signaling.onReleaseFloor()
        called.join()
    }

    @Suppress("SameParameterValue")
    private fun read(fileName: String) =
        FileSystem.RESOURCES.read(fileName.toPath(), BufferedSource::readUtf8)
}
