package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse
import com.pexip.sdk.media.IceServer
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            participantStep = object : TestParticipantTest() {},
            iceServers = iceServers
        )
        assertEquals(iceServers, signaling.iceServers)
    }

    @Test
    fun `onOffer() returns Answer (first call)`() {
        var ackCalled = false
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMix = Random.nextBoolean()
        val response = CallsResponse(
            callId = UUID.randomUUID(),
            sdp = Random.nextString(8)
        )
        val callStep = object : TestCallStep() {

            override fun ack(token: String): Call<Unit> = object : TestCall<Unit> {
                override fun execute() {
                    assertEquals(store.get().token, token)
                    ackCalled = true
                }
            }
        }
        val participantStep = object : TestParticipantTest() {

            override fun calls(request: CallsRequest, token: String): Call<CallsResponse> =
                object : TestCall<CallsResponse> {

                    override fun execute(): CallsResponse {
                        assertEquals(sdp, request.sdp)
                        assertEquals(
                            expected = if (presentationInMix) "main" else null,
                            actual = request.present,
                        )
                        assertEquals(callType, request.callType)
                        assertEquals(store.get().token, token)
                        return response
                    }
                }

            override fun call(callId: UUID): InfinityService.CallStep {
                assertEquals(response.callId, callId)
                return callStep
            }
        }
        val signaling = RealMediaConnectionSignaling(store, participantStep, iceServers)
        assertEquals(signaling.onOffer(callType, sdp, presentationInMix), response.sdp)
        assertEquals(callStep, signaling.callStep)
        assertEquals(mapOf("ToQx" to "jSThfoPwGg6gKmxeTmTqz8ea"), signaling.pwds)
        assertTrue(ackCalled)
    }

    @Test
    fun `onOffer() returns Answer (subsequent calls)`() {
        val callType = Random.nextString(8)
        val sdp = read("session_description_original")
        val presentationInMix = Random.nextBoolean()
        val response = UpdateResponse(Random.nextString(8))
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantTest() {},
            iceServers = iceServers
        )
        signaling.callStep = object : TestCallStep() {
            override fun update(request: UpdateRequest, token: String): Call<UpdateResponse> =
                object : TestCall<UpdateResponse> {
                    override fun execute(): UpdateResponse {
                        assertEquals(sdp, request.sdp)
                        assertEquals(store.get().token, token)
                        return response
                    }
                }
        }
        assertEquals(signaling.onOffer(callType, sdp, presentationInMix), response.sdp)
        assertEquals(mapOf("ToQx" to "jSThfoPwGg6gKmxeTmTqz8ea"), signaling.pwds)
    }

    @Test
    fun `onCandidate() returns`() {
        var called = false
        val candidate =
            "candidate:842163049 1 udp 1686052607 45.83.220.205 49922 typ srflx raddr 10.0.2.16 rport 43359 generation 0 ufrag /GLA network-id 5 network-cost 10"
        val mid = Random.nextString(8)
        val ufrag = "/GLA"
        val pwd = Random.nextString(8)
        val signaling = RealMediaConnectionSignaling(
            store = store,
            participantStep = object : TestParticipantTest() {},
            iceServers = iceServers
        )
        signaling.callStep = object : TestCallStep() {
            override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> =
                object : TestCall<Unit> {
                    override fun execute() {
                        assertEquals(candidate, request.candidate)
                        assertEquals(mid, request.mid)
                        assertEquals(ufrag, request.ufrag)
                        assertEquals(pwd, request.pwd)
                        assertEquals(store.get().token, token)
                        called = true
                    }
                }
        }
        signaling.pwds = mapOf(ufrag to pwd)
        signaling.onCandidate(candidate, mid)
        assertTrue(called)
    }

    @Test
    fun `onAudioMuted() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun mute(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onAudioMuted()
        assertTrue(called)
    }

    @Test
    fun `onAudioUnmuted() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun unmute(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onAudioUnmuted()
        assertTrue(called)
    }

    @Test
    fun `onVideoMuted() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun videoMuted(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onVideoMuted()
        assertTrue(called)
    }

    @Test
    fun `onVideoUnmuted() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun videoUnmuted(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onVideoUnmuted()
        assertTrue(called)
    }

    @Test
    fun `onTakeFloor() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun takeFloor(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onTakeFloor()
        assertTrue(called)
    }

    @Test
    fun `onReleaseFloor() returns`() {
        var called = false
        val step = object : TestParticipantTest() {

            override fun releaseFloor(token: String): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(store.get().token, token)
                    called = true
                }
            }
        }
        val signaling = RealMediaConnectionSignaling(store, step, iceServers)
        signaling.onReleaseFloor()
        assertTrue(called)
    }

    @Suppress("SameParameterValue")
    private fun read(fileName: String) = FileSystem.RESOURCES.read(fileName.toPath()) { readUtf8() }
}
