package com.pexip.sdk.media

public interface LocalVideoTrack : LocalMediaTrack, VideoTrack {

    public fun startCapture(profile: QualityProfile)
}
