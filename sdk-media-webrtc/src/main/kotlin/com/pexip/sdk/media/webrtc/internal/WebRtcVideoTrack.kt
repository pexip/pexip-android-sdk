package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.Renderer
import com.pexip.sdk.media.VideoTrack
import org.webrtc.VideoSink

@JvmInline
internal value class WebRtcVideoTrack(private val videoTrack: org.webrtc.VideoTrack) : VideoTrack {

    override fun addRenderer(renderer: Renderer) {
        require(renderer is VideoSink) { "renderer must be an instance of VideoSink." }
        videoTrack.addSink(renderer)
    }

    override fun removeRenderer(renderer: Renderer) {
        require(renderer is VideoSink) { "renderer must be an instance of VideoSink." }
        videoTrack.removeSink(renderer)
    }
}
