/*
 * Copyright 2022-2023 Pexip AS
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
import android.util.Rational
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.JavaI420Buffer
import org.webrtc.JniCommon
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

        private var width = 0
        private var height = 0
        private var rotation = 0

        override fun onCapturerStarted(success: Boolean) {
            videoSource.capturerObserver.onCapturerStarted(success)
            if (capturing == success) return
            capturing = success
            notify(success)
        }

        override fun onCapturerStopped() {
            val rational = Rational(width, height)
            val buffer = createBlackBuffer(rational.numerator * 16, rational.denominator * 16)
            val frame = VideoFrame(buffer, rotation, 0)
            videoSource.capturerObserver.onFrameCaptured(frame)
            frame.release()
            videoSource.capturerObserver.onCapturerStopped()
            if (!capturing) return
            capturing = false
            notify(false)
        }

        override fun onFrameCaptured(frame: VideoFrame) {
            width = frame.buffer.width
            height = frame.buffer.height
            rotation = frame.rotation
            videoSource.capturerObserver.onFrameCaptured(frame)
        }

        private fun notify(capturing: Boolean) {
            signalingExecutor.maybeExecute {
                capturingListeners.forEach {
                    it.safeOnCapturing(capturing)
                }
            }
        }

        private fun createBlackBuffer(width: Int, height: Int): VideoFrame.Buffer {
            val chromaHeight = (height + 1) / 2
            val strideUV = (width + 1) / 2
            val yPos = 0
            val uPos = yPos + width * height
            val vPos = uPos + strideUV * chromaHeight
            val size = width * height + 2 * strideUV * chromaHeight
            val buffer = JniCommon.nativeAllocateByteBuffer(size)
            buffer.put(ByteArray(size) { if (it < uPos) 16 else Byte.MIN_VALUE })
            buffer.position(yPos)
            buffer.limit(uPos)
            val dataY = buffer.slice()
            buffer.position(uPos)
            buffer.limit(vPos)
            val dataU = buffer.slice()
            buffer.position(vPos)
            buffer.limit(vPos + strideUV * chromaHeight)
            val dataV = buffer.slice()
            val callback = Runnable { JniCommon.nativeFreeByteBuffer(buffer) }
            return JavaI420Buffer.wrap(
                width,
                height,
                dataY,
                width,
                dataU,
                strideUV,
                dataV,
                strideUV,
                callback,
            )
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
