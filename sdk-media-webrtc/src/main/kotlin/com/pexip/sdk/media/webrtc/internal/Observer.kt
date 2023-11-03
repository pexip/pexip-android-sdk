/*
 * Copyright 2023 Pexip AS
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
package com.pexip.sdk.media.webrtc.internal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.IceCandidateErrorEvent
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.ContinualGatheringPolicy
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import java.util.concurrent.atomic.AtomicBoolean

internal class Observer(private val continualGatheringPolicy: ContinualGatheringPolicy) :
    PeerConnection.Observer, DataChannel.Observer {

    private val started = AtomicBoolean()
    private val candidates = mutableMapOf<String, IceCandidate>()
    private val _event = MutableSharedFlow<Event>(extraBufferCapacity = 64)

    val event = _event.asSharedFlow()

    fun start() {
        if (started.compareAndSet(false, true)) {
            _event.tryEmit(Event.OnRenegotiationNeeded)
        }
    }

    fun stop() {
        started.set(false)
    }

    override fun onBufferedAmountChange(previousAmount: Long) {
    }

    override fun onStateChange() {
    }

    override fun onMessage(buffer: DataChannel.Buffer) {
        _event.tryEmit(Event.OnMessage(buffer.data))
    }

    override fun onRenegotiationNeeded() {
        if (started.get()) {
            _event.tryEmit(Event.OnRenegotiationNeeded)
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        if (continualGatheringPolicy == ContinualGatheringPolicy.GATHER_ONCE) {
            if (candidate.sdpMid in candidates) return
            candidates[candidate.sdpMid] =
                IceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, "")
        }
        _event.tryEmit(Event.OnIceCandidate(candidate))
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        if (continualGatheringPolicy == ContinualGatheringPolicy.GATHER_ONCE) {
            if (state == PeerConnection.IceGatheringState.COMPLETE) {
                candidates.forEach { (_, candidate) ->
                    _event.tryEmit(Event.OnIceCandidate(candidate))
                }
                candidates.clear()
            }
        }
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) {
        _event.tryEmit(Event.OnIceConnectionChange(newState))
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
        _event.tryEmit(Event.OnAddTrack(receiver))
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        _event.tryEmit(Event.OnRemoveTrack(receiver))
    }

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        _event.tryEmit(Event.OnSignalingChange(state))
    }

    override fun onAddStream(stream: MediaStream) {
    }

    override fun onRemoveStream(stream: MediaStream) {
    }

    override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {
    }

    override fun onDataChannel(channel: DataChannel) {
    }

    override fun onIceConnectionReceivingChange(changing: Boolean) {
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
    }

    override fun onTrack(transceiver: RtpTransceiver) {
    }
}
