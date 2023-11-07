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
package com.pexip.sdk.media

import kotlinx.coroutines.flow.Flow

/**
 * Represents a signaling component of [MediaConnection].
 *
 * @property iceServers a list of available [IceServer]s
 * @property iceTransportsRelayOnly whether relay only mode should be used
 * @property event a [Flow] of [SignalingEvent]s
 * @property dataChannel an optional [DataChannel] for messaging between peers
 */
public interface MediaConnectionSignaling {

    public val iceServers: List<IceServer>

    public val iceTransportsRelayOnly: Boolean

    public val event: Flow<SignalingEvent>

    public val dataChannel: DataChannel?

    /**
     * Invoked when an offer is available.
     *
     * @param callType a call type (currently only "WEBRTC" is supported)
     * @param description an offer, usually represented by an SDP
     * @param presentationInMain whether presentation should be embedded in main video feed
     * @param fecc whether far end camera control should be enabled
     * @return an answer, may be null if in a direct media call
     */
    public suspend fun onOffer(
        callType: String,
        description: String,
        presentationInMain: Boolean,
        fecc: Boolean,
    ): String?

    /**
     * Invoked when the client wants to ignore the offer in a direct media call
     */
    public suspend fun onOfferIgnored()

    /**
     * Invoked when answer is ready to be sent in a direct media call
     *
     * @param description an answer, usually represented by an SDP
     */
    public suspend fun onAnswer(description: String)

    /**
     * Invoked when offer is set and the connection is ready to accept media.
     */
    public suspend fun onAck()

    /**
     * Invoked when a new ICE candidate is available.
     *
     * @param candidate an ICE candidate
     * @param mid a media ID associated with this candidate
     * @param ufrag a username fragment of this ICE candidate
     * @param pwd a password of this ICE candidate
     */
    public suspend fun onCandidate(candidate: String, mid: String, ufrag: String, pwd: String)

    /**
     * Invoked when a sequence of DTMF digits must be sent.
     *
     * @param digits a sequence of DTMF digits
     */
    public suspend fun onDtmf(digits: String)

    /**
     * Invoked when audio is muted.
     */
    public suspend fun onAudioMuted()

    /**
     * Invoked when audio is unmuted.
     */
    public suspend fun onAudioUnmuted()

    /**
     * Invoked when video is muted.
     */
    public suspend fun onVideoMuted()

    /**
     * Invoked when video is unmuted.
     */
    public suspend fun onVideoUnmuted()

    /**
     * Invoked when local presentation feed is added.
     */
    public suspend fun onTakeFloor()

    /**
     * Invoked when local presentation feed is removed.
     */
    public suspend fun onReleaseFloor()

    /**
     * Invoked when [Data] is received.
     */
    public suspend fun onData(data: Data)

    /**
     * Attaches [DataSender] to this [MediaConnectionSignaling]
     *
     * @param sender a [DataSender] to attach
     */
    public suspend fun attach(sender: DataSender)

    /**
     * Detaches [DataSender] from this [MediaConnectionSignaling]
     *
     * @param sender a [DataSender] to detach
     */
    public suspend fun detach(sender: DataSender)
}
