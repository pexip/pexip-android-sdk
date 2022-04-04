package com.pexip.sdk.media.webrtc

import org.webrtc.VideoTrack

public interface VideoTrackListener {

    public fun onVideoTrack(videoTrack: VideoTrack?)
}
