package com.pexip.sdk.media.webrtc.internal

import android.view.SurfaceView
import com.pexip.sdk.media.VideoTrack
import org.webrtc.SurfaceViewRenderer

internal open class WebRtcVideoTrack(internal val videoTrack: org.webrtc.VideoTrack) : VideoTrack {

    override fun addRenderer(renderer: SurfaceView) {
        require(renderer is SurfaceViewRenderer) { "renderer must be an instance of SurfaceViewRenderer." }
        videoTrack.addSink(renderer)
    }

    override fun removeRenderer(renderer: SurfaceView) {
        require(renderer is SurfaceViewRenderer) { "renderer must be an instance of SurfaceViewRenderer." }
        videoTrack.removeSink(renderer)
    }
}
