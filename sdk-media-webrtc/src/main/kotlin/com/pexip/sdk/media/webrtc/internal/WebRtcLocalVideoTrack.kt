package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

internal open class WebRtcLocalVideoTrack(
    applicationContext: Context,
    private val textureHelper: SurfaceTextureHelper,
    private val videoCapturer: VideoCapturer,
    private val videoSource: VideoSource,
    videoTrack: VideoTrack,
    protected val workerExecutor: Executor,
    protected val signalingExecutor: Executor,
) : LocalVideoTrack, WebRtcVideoTrack(videoTrack) {

    private val disposed = AtomicBoolean()
    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()
    private val capturerObserver = object : CapturerObserver {

        override fun onCapturerStarted(success: Boolean) {
            videoSource.capturerObserver.onCapturerStarted(success)
            if (capturing == success) return
            capturing = success
            signalingExecutor.maybeExecute {
                capturingListeners.forEach {
                    it.safeOnCapturing(success)
                }
            }
        }

        override fun onCapturerStopped() {
            videoSource.capturerObserver.onCapturerStopped()
            if (!capturing) return
            capturing = false
            signalingExecutor.maybeExecute {
                capturingListeners.forEach {
                    it.safeOnCapturing(false)
                }
            }
        }

        override fun onFrameCaptured(frame: VideoFrame?) {
            videoSource.capturerObserver.onFrameCaptured(frame)
        }
    }

    @Volatile
    private var capturing = false

    init {
        workerExecutor.maybeExecute {
            videoCapturer.initialize(textureHelper, applicationContext, capturerObserver)
        }
    }

    override fun startCapture(profile: QualityProfile) {
        workerExecutor.maybeExecute {
            videoCapturer.startCapture(profile.width, profile.height, profile.fps)
        }
    }

    override fun startCapture() {
        startCapture(QualityProfile.Medium)
    }

    override fun stopCapture() {
        workerExecutor.maybeExecute {
            videoCapturer.stopCapture()
        }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        signalingExecutor.maybeExecute {
            listener.safeOnCapturing(capturing)
        }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                videoCapturer.stopCapture()
                videoTrack.dispose()
                videoSource.dispose()
                videoCapturer.dispose()
                textureHelper.dispose()
            }
        }
    }
}
