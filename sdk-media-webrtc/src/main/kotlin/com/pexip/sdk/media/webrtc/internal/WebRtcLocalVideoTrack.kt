package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CopyOnWriteArraySet

internal open class WebRtcLocalVideoTrack(
    applicationContext: Context,
    private val textureHelper: SurfaceTextureHelper,
    private val videoCapturer: VideoCapturer,
    private val videoSource: VideoSource,
    videoTrack: VideoTrack,
) : LocalVideoTrack, WebRtcVideoTrack(videoTrack) {

    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val capturingListeners = CopyOnWriteArraySet<LocalVideoTrack.CapturingListener>()
    private val capturerObserver = object : CapturerObserver {

        override fun onCapturerStarted(success: Boolean) {
            videoSource.capturerObserver.onCapturerStarted(success)
            if (capturing == success) return
            handler.post {
                capturing = success
                capturingListeners.forEach { it.onCapturing(success) }
            }
        }

        override fun onCapturerStopped() {
            videoSource.capturerObserver.onCapturerStopped()
            if (!capturing) return
            handler.post {
                capturing = false
                capturingListeners.forEach { it.onCapturing(false) }
            }
        }

        override fun onFrameCaptured(frame: VideoFrame?) {
            videoSource.capturerObserver.onFrameCaptured(frame)
        }
    }

    @Volatile
    private var capturing = false

    init {
        videoCapturer.initialize(textureHelper, applicationContext, capturerObserver)
    }

    override fun startCapture(profile: QualityProfile) {
        videoCapturer.startCapture(profile.width, profile.height, profile.fps)
    }

    override fun stopCapture() {
        videoCapturer.stopCapture()
    }

    override fun registerCapturingListener(listener: LocalVideoTrack.CapturingListener) {
        handler.post { listener.onCapturing(capturing) }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalVideoTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        videoTrack.dispose()
        videoSource.dispose()
        videoCapturer.dispose()
        textureHelper.dispose()
        handler.removeCallbacksAndMessages(null)
    }
}
