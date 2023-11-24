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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.AckRequest
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateEvent
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.NewOfferEvent
import com.pexip.sdk.api.infinity.PeerDisconnectEvent
import com.pexip.sdk.api.infinity.PreferredAspectRatioRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateSdpEvent
import com.pexip.sdk.media.CandidateSignalingEvent
import com.pexip.sdk.media.Data
import com.pexip.sdk.media.DataSender
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.media.OfferSignalingEvent
import com.pexip.sdk.media.RestartSignalingEvent
import com.pexip.sdk.media.SignalingEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

internal class MediaConnectionSignalingImpl(
    private val store: TokenStore,
    event: Flow<Event>,
    private val participantStep: InfinityService.ParticipantStep,
    override val directMedia: Boolean,
    override val iceServers: List<IceServer>,
    override val iceTransportsRelayOnly: Boolean,
    override val dataChannel: DataChannelImpl?,
    callStep: InfinityService.CallStep? = null,
) : MediaConnectionSignaling {

    val callStep = when (callStep) {
        null -> CompletableDeferred()
        else -> CompletableDeferred(callStep)
    }

    override val event: Flow<SignalingEvent> = event.mapNotNull(::toSignalingEvent)

    override suspend fun onOffer(
        callType: String,
        description: String,
        presentationInMain: Boolean,
        fecc: Boolean,
    ): String? {
        val token = store.get()
        val step = when (callStep.isCompleted) {
            true -> callStep.await()
            else -> null
        }
        val response = when (step) {
            null -> {
                val request = CallsRequest(
                    callType = callType,
                    sdp = description,
                    present = if (presentationInMain) "main" else null,
                    fecc = fecc,
                )
                val response = participantStep.calls(request, token).await()
                callStep.complete(participantStep.call(response.callId))
                response
            }
            else -> {
                val request = UpdateRequest(
                    sdp = description,
                    fecc = fecc,
                )
                step.update(request, token).await()
            }
        }
        return if (response.offerIgnored || response.sdp.isBlank()) null else response.sdp
    }

    override suspend fun onOfferIgnored() {
        val callStep = callStep.await()
        val token = store.get()
        val request = AckRequest(offerIgnored = true)
        callStep.ack(request, token).await()
    }

    override suspend fun onAnswer(description: String) {
        val callStep = callStep.await()
        val token = store.get()
        val request = AckRequest(sdp = description)
        callStep.ack(request, token).await()
    }

    override suspend fun onAck() {
        val callStep = callStep.await()
        val token = store.get()
        callStep.ack(token).await()
    }

    override suspend fun onCandidate(candidate: String, mid: String, ufrag: String, pwd: String) {
        val callStep = callStep.await()
        val token = store.get()
        val request = NewCandidateRequest(
            candidate = candidate,
            mid = mid,
            ufrag = ufrag,
            pwd = pwd,
        )
        callStep.newCandidate(request, token).await()
    }

    override suspend fun onDtmf(digits: String) {
        val callStep = callStep.await()
        val request = DtmfRequest(digits)
        val token = store.get()
        callStep.dtmf(request, token).await()
    }

    override suspend fun onAudioMuted() {
        participantStep.mute(store.get()).await()
    }

    override suspend fun onAudioUnmuted() {
        participantStep.unmute(store.get()).await()
    }

    override suspend fun onVideoMuted() {
        participantStep.videoMuted(store.get()).await()
    }

    override suspend fun onVideoUnmuted() {
        participantStep.videoUnmuted(store.get()).await()
    }

    override suspend fun onTakeFloor() {
        participantStep.takeFloor(store.get()).await()
    }

    override suspend fun onReleaseFloor() {
        participantStep.releaseFloor(store.get()).await()
    }

    override suspend fun onPreferredAspectRatio(aspectRatio: Float) {
        try {
            val request = PreferredAspectRatioRequest(aspectRatio.coerceIn(0f, 2f))
            val token = store.get()
            participantStep.preferredAspectRatio(request, token).await()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            // noop
        }
    }

    override suspend fun attach(sender: DataSender) = dataChannel?.attach(sender) ?: Unit

    override suspend fun detach(sender: DataSender) = dataChannel?.detach(sender) ?: Unit

    override suspend fun onData(data: Data) = dataChannel?.onData(data) ?: Unit

    private fun toSignalingEvent(event: Event) = when (event) {
        is NewOfferEvent -> OfferSignalingEvent(event.sdp)
        is UpdateSdpEvent -> OfferSignalingEvent(event.sdp)
        is NewCandidateEvent -> CandidateSignalingEvent(event.mid, event.candidate)
        is PeerDisconnectEvent -> RestartSignalingEvent
        else -> null
    }
}
