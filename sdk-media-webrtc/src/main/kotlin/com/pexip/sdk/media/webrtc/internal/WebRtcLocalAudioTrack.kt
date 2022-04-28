package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.LocalAudioTrack

internal class WebRtcLocalAudioTrack(
    private val audioSource: org.webrtc.AudioSource,
    internal val audioTrack: org.webrtc.AudioTrack,
) : LocalAudioTrack {

    override fun dispose() {
        audioTrack.dispose()
        audioSource.dispose()
    }
}
