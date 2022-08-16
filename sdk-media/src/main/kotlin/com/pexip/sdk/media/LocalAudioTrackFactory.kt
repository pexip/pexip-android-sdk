package com.pexip.sdk.media

public interface LocalAudioTrackFactory {

    /**
     * Creates a [LocalAudioTrack].
     *
     * @return a [LocalAudioTrack]
     * @throws IllegalStateException if [LocalAudioTrackFactory] has been disposed
     */
    public fun createLocalAudioTrack(): LocalAudioTrack
}
