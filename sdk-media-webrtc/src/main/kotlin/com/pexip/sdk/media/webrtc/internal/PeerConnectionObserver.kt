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
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import java.util.concurrent.atomic.AtomicBoolean

internal class PeerConnectionObserver : PeerConnection.Observer {

    private val onRenegotiationNeeded = AtomicBoolean(false)
    private val _event = MutableSharedFlow<Event>(extraBufferCapacity = 64)

    val event = _event.asSharedFlow()

    override fun onRenegotiationNeeded() {
        if (onRenegotiationNeeded.compareAndSet(false, true)) return
        _event.tryEmit(Event.OnRenegotiationNeeded)
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        _event.tryEmit(Event.OnIceCandidate(candidate))
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        _event.tryEmit(Event.OnIceGatheringChange(state))
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

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
    }

    override fun onTrack(transceiver: RtpTransceiver) {
    }
}
