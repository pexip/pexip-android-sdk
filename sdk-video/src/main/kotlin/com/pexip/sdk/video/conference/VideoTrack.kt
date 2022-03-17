package com.pexip.sdk.video.conference

import org.webrtc.EglBase
import org.webrtc.VideoTrack as WebRtcVideoTrack

public class VideoTrack(
    internal val context: EglBase.Context,
    internal val value: WebRtcVideoTrack,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VideoTrack) return false
        if (context != other.context) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
