package com.pexip.sdk.media

/**
 * A local video track.
 */
public interface LocalVideoTrack : LocalMediaTrack, VideoTrack {

    /**
     * Starts the capture with a specific [QualityProfile].
     *
     * @param profile a quality profile to use when capturing
     */
    public fun startCapture(profile: QualityProfile)
}
