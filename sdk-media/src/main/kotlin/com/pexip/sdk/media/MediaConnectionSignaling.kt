package com.pexip.sdk.media

public interface MediaConnectionSignaling {

    public fun onOffer(callType: String, description: String, presentationInMix: Boolean): String

    public fun onCandidate(candidate: String, mid: String)

    public fun onVideoMuted()

    public fun onVideoUnmuted()
}
