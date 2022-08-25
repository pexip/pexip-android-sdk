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
     * @return an answer
     */
    public fun onOffer(callType: String, description: String, presentationInMain: Boolean): String

    /**
     * Invoked when a new ICE candidate is available.
     *
     * @param candidate an ICE candidate
     * @param mid a media ID associated with this candidate
     */
    public fun onCandidate(candidate: String, mid: String)

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
