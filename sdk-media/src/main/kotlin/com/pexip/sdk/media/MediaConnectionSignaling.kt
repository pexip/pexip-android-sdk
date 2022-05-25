package com.pexip.sdk.media

public interface MediaConnectionSignaling {

    public val iceServers: List<IceServer>

    public fun onOffer(callType: String, description: String, presentationInMix: Boolean): String

    public fun onCandidate(candidate: String, mid: String)

    public fun onAudioMuted()

    public fun onAudioUnmuted()

    public fun onVideoMuted()

    public fun onVideoUnmuted()
}
