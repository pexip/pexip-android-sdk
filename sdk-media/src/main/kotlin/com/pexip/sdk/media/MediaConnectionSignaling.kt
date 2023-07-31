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

/**
 * Represents a signaling component of [MediaConnection].
 */
public interface MediaConnectionSignaling {

    /**
     * A list of available [IceServer]s.
     */
    public val iceServers: List<IceServer>

    /**
     * Invoked when an offer is available.
     *
     * @param callType a call type (currently only "WEBRTC" is supported)
     * @param description an offer, usually represented by an SDP
     * @param presentationInMain whether presentation should be embedded in main video feed
     * @param fecc whether far end camera control should be enabled
     * @return an answer
     */
    public fun onOffer(
        callType: String,
        description: String,
        presentationInMain: Boolean,
        fecc: Boolean,
    ): String

    /**
     * Invoked when offer is set and the connection is ready to accept media.
     */
    public fun onAck()

    /**
     * Invoked when a new ICE candidate is available.
     *
     * @param candidate an ICE candidate
     * @param mid a media ID associated with this candidate
     * @param ufrag a username fragment of this ICE candidate
     * @param pwd a password of this ICE candidate
     */
    public fun onCandidate(candidate: String, mid: String, ufrag: String, pwd: String)

    /**
     * Invoked when a sequence of DTMF digits must be sent.
     *
     * @param digits a sequence of DTMF digits
     */
    public fun onDtmf(digits: String)

    /**
     * Invoked when audio is muted.
     */
    public fun onAudioMuted()

    /**
     * Invoked when audio is unmuted.
     */
    public fun onAudioUnmuted()

    /**
     * Invoked when video is muted.
     */
    public fun onVideoMuted()

    /**
     * Invoked when video is unmuted.
     */
    public fun onVideoUnmuted()

    /**
     * Invoked when local presentation feed is added.
     */
    public fun onTakeFloor()

    /**
     * Invoked when local presentation feed is removed.
     */
    public fun onReleaseFloor()
}
