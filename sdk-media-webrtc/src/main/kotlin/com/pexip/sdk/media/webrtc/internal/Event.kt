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

import com.pexip.sdk.media.Data
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.SignalingState
import org.webrtc.RtpReceiver

internal sealed interface Event {

    data object OnRenegotiationNeeded : Event

    @JvmInline
    value class OnAddTrack(override val receiver: RtpReceiver) : Event, RtpReceiverOwner

    @JvmInline
    value class OnRemoveTrack(override val receiver: RtpReceiver) : Event, RtpReceiverOwner

    @JvmInline
    value class OnIceCandidate(val candidate: IceCandidate) : Event

    @JvmInline
    value class OnIceConnectionChange(val state: PeerConnection.IceConnectionState) : Event

    @JvmInline
    value class OnSignalingChange(val state: SignalingState) : Event

    @JvmInline
    value class OnData(val data: Data) : Event
}
