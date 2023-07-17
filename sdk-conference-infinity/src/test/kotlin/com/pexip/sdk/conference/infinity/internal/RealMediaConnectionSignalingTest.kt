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

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse
import com.pexip.sdk.media.IceServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RealMediaConnectionSignalingTest {

    private lateinit var store: TokenStore
    private lateinit var iceServers: List<IceServer>

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
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
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
        )
        assertEquals(iceServers, signaling.iceServers)
    }

    @Test
    fun `onOffer() returns Answer (first call)`() = runTest {
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMix = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val response = CallsResponse(
            callId = UUID.randomUUID(),
            sdp = Random.nextString(8),
        )
        val callStep = object : TestCallStep() {}
        val participantStep = object : TestParticipantStep() {
            override fun calls(request: CallsRequest, token: String): Call<CallsResponse> =
                object : TestCall<CallsResponse> {
                    override fun enqueue(callback: Callback<CallsResponse>) {
                        assertEquals(sdp, request.sdp)
                        assertEquals(
                            expected = if (presentationInMix) "main" else null,
                            actual = request.present,
                        )
                        assertEquals(callType, request.callType)
                        assertEquals(store.get().token, token)
                        callback.onSuccess(this, response)
                    }
                }

            override fun call(callId: UUID): InfinityService.CallStep {
                assertEquals(response.callId, callId)
                return callStep
            }
        }
        val signaling = RealMediaConnectionSignaling(store, participantStep, iceServers)
        assertEquals(
            expected = signaling.onOffer(
                callType = callType,
                description = sdp,
                presentationInMain = presentationInMix,
                fecc = fecc,
            ),
            actual = response.sdp,
        )
        assertEquals(callStep, signaling.callStep.await())
    }

    @Test
    fun `onOffer() returns Answer (subsequent calls)`() = runTest {
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMix = Random.nextBoolean()
        val fecc = Random.nextBoolean()
        val response = UpdateResponse(Random.nextString(8))
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun update(request: UpdateRequest, token: String): Call<UpdateResponse> =
                    object : TestCall<UpdateResponse> {
                        override fun enqueue(callback: Callback<UpdateResponse>) {
                            assertEquals(sdp, request.sdp)
                            assertEquals(store.get().token, token)
                            callback.onSuccess(this, response)
                        }
                    }
            },
        )
        assertEquals(
            expected = signaling.onOffer(
                callType = callType,
                description = sdp,
                presentationInMain = presentationInMix,
                fecc = fecc,
            ),
            actual = response.sdp,
        )
    }

    @Test
    fun `onAck() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
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
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> =
                    object : TestCall<Unit> {
                        override fun enqueue(callback: Callback<Unit>) {
                            assertEquals(candidate, request.candidate)
                            assertEquals(mid, request.mid)
                            assertEquals(ufrag, request.ufrag)
                            assertEquals(pwd, request.pwd)
                            assertEquals(store.get().token, token)
                            called.complete()
                            callback.onSuccess(this, Unit)
                        }
                    }
            },
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
            participantStep = object : TestParticipantStep() {},
            iceServers = iceServers,
            callStep = object : TestCallStep() {
                override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> =
                    object : TestCall<Boolean> {
                        override fun enqueue(callback: Callback<Boolean>) {
                            assertEquals(digits, request.digits)
                            called.complete()
                            callback.onSuccess(this, result)
                        }
                    }
            },
        )
        signaling.onDtmf(digits)
        called.join()
    }

    @Test
    fun `onAudioMuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun mute(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onAudioMuted()
        called.join()
    }

    @Test
    fun `onAudioUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun unmute(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onAudioUnmuted()
        called.join()
    }

    @Test
    fun `onVideoMuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun videoMuted(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onVideoMuted()
        called.join()
    }

    @Test
    fun `onVideoUnmuted() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun videoUnmuted(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onVideoUnmuted()
        called.join()
    }

    @Test
    fun `onTakeFloor() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun takeFloor(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onTakeFloor()
        called.join()
    }

    @Test
    fun `onReleaseFloor() returns`() = runTest {
        val called = Job()
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantStep() {
                override fun releaseFloor(token: String): Call<Unit> = object : TestCall<Unit> {
                    override fun enqueue(callback: Callback<Unit>) {
                        assertEquals(store.get().token, token)
                        called.complete()
                        callback.onSuccess(this, Unit)
                    }
                }
            },
            iceServers = iceServers,
        )
        signaling.onReleaseFloor()
        called.join()
    }

    @Suppress("SameParameterValue")
    private fun read(fileName: String) = FileSystem.RESOURCES.read(fileName.toPath()) { readUtf8() }
}
