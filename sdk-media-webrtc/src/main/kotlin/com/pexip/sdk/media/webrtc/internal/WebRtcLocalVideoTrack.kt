/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

internal open class WebRtcLocalVideoTrack(
    applicationContext: Context,
    eglBase: EglBase?,
    private val videoCapturer: VideoCapturer,
    private val videoSource: VideoSource,
    internal val videoTrack: org.webrtc.VideoTrack,
    protected val workerExecutor: Executor,
    protected val signalingExecutor: Executor,
) : LocalVideoTrack, VideoTrack by WebRtcVideoTrack(videoTrack) {

    private val disposed = AtomicBoolean()
    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()
    private val textureHelper =
        SurfaceTextureHelper.create("CaptureThread:${videoTrack.id()}", eglBase?.eglBaseContext)
    private val capturerObserver = object : CapturerObserver {

        override fun onCapturerStarted(success: Boolean) {
            videoSource.capturerObserver.onCapturerStarted(success)
            if (capturing == success) return
            capturing = success
            notify(success)
        }

        override fun onCapturerStopped() {
            videoSource.capturerObserver.onCapturerStopped()
            if (!capturing) return
            capturing = false
            notify(false)
        }

        override fun onFrameCaptured(frame: VideoFrame?) {
            videoSource.capturerObserver.onFrameCaptured(frame)
        }

        private fun notify(capturing: Boolean) {
            signalingExecutor.maybeExecute {
                capturingListeners.forEach {
                    it.safeOnCapturing(capturing)
                }
            }
        }
    }

    @Volatile
    final override var capturing = false
        private set

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
