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
package com.pexip.sdk.media.webrtc

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.projection.MediaProjection
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.CameraVideoTrackFactory
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalAudioTrackFactory
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.android.MediaProjectionVideoTrackFactory
import com.pexip.sdk.media.webrtc.internal.PeerConnectionWrapper
import com.pexip.sdk.media.webrtc.internal.SimpleCameraEventsHandler
import com.pexip.sdk.media.webrtc.internal.WebRtcCameraVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcMediaConnection
import com.pexip.sdk.media.webrtc.internal.from
import com.pexip.sdk.media.webrtc.internal.maybeExecute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.VideoCapturer
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.UUID

public class WebRtcMediaConnectionFactory private constructor(
    private val applicationContext: Context,
    private val eglBase: EglBase?,
    private val cameraEnumerator: CameraEnumerator,
    private val audioDeviceModule: AudioDeviceModule,
    videoDecoderFactory: VideoDecoderFactory,
    videoEncoderFactory: VideoEncoderFactory,
) : MediaConnectionFactory,
    LocalAudioTrackFactory,
    CameraVideoTrackFactory,
    MediaProjectionVideoTrackFactory {

    @Deprecated(
        message = "Use WebRtcMediaConnectionFactory.Builder instead.",
        level = DeprecationLevel.ERROR,
    )
    @JvmOverloads
    public constructor(
        context: Context,
        eglBase: EglBase,
        cameraEnumerator: CameraEnumerator = CameraEnumerator(context),
        videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglBase),
        videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(eglBase),
    ) : this(
        applicationContext = context.applicationContext,
        eglBase = eglBase,
        cameraEnumerator = cameraEnumerator,
        audioDeviceModule = JavaAudioDeviceModule(context),
        videoDecoderFactory = videoDecoderFactory,
        videoEncoderFactory = videoEncoderFactory,
    )

    private val factory = PeerConnectionFactory.builder()
        .setAudioDeviceModule(audioDeviceModule)
        .setVideoDecoderFactory(videoDecoderFactory)
        .setVideoEncoderFactory(videoEncoderFactory)
        .createPeerConnectionFactory()

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val workerDispatcher = newSingleThreadContext("WebRtcMediaConnectionFactory")
    private val signalingDispatcher = Dispatchers.Main.immediate
    private val job = Job()

    override fun getDeviceNames(): List<String> = cameraEnumerator.deviceNames.toList()

    override fun isFrontFacing(deviceName: String): Boolean {
        checkDeviceName(deviceName)
        return cameraEnumerator.isFrontFacing(deviceName)
    }

    override fun isBackFacing(deviceName: String): Boolean {
        checkDeviceName(deviceName)
        return cameraEnumerator.isBackFacing(deviceName)
    }

    override fun getQualityProfiles(deviceName: String): List<QualityProfile> {
        checkDeviceName(deviceName)
        return cameraEnumerator.getSupportedFormats(deviceName).map(QualityProfile::from)
    }

    override fun createLocalAudioTrack(): LocalAudioTrack {
        checkNotDisposed()
        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack(createMediaTrackId(), audioSource)
        return WebRtcLocalAudioTrack(
            context = applicationContext,
            audioSource = audioSource,
            audioTrack = audioTrack,
            scope = CoroutineScope(),
            signalingDispatcher = signalingDispatcher,
        )
    }

    @Deprecated(
        message = "Use createCameraVideoTrack() that also accepts a Callback.",
        level = DeprecationLevel.ERROR,
    )
    override fun createCameraVideoTrack(): CameraVideoTrack {
        throw UnsupportedOperationException()
    }

    @Deprecated(
        message = "Use createCameraVideoTrack() that also accepts a Callback.",
        level = DeprecationLevel.ERROR,
    )
    override fun createCameraVideoTrack(deviceName: String): CameraVideoTrack {
        throw UnsupportedOperationException()
    }

    override fun createCameraVideoTrack(callback: CameraVideoTrack.Callback): CameraVideoTrack {
        checkNotDisposed()
        val deviceNames = cameraEnumerator.deviceNames
        val deviceName = deviceNames.firstOrNull(cameraEnumerator::isFrontFacing)
            ?: deviceNames.firstOrNull(cameraEnumerator::isBackFacing)
            ?: deviceNames.firstOrNull()
        return createCameraVideoTrack(checkNotNull(deviceName) { "No available camera." }, callback)
    }

    override fun createCameraVideoTrack(
        deviceName: String,
        callback: CameraVideoTrack.Callback,
    ): CameraVideoTrack {
        checkNotDisposed()
        checkDeviceName(deviceName)
        val handler = SimpleCameraEventsHandler(callback, signalingDispatcher.asExecutor())
        val videoCapturer = cameraEnumerator.createCapturer(deviceName, handler)
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        return WebRtcCameraVideoTrack(
            factory = this,
            applicationContext = applicationContext,
            eglBase = eglBase,
            deviceName = deviceName,
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource),
            scope = CoroutineScope(),
            signalingDispatcher = signalingDispatcher,
        )
    }

    override fun createMediaProjectionVideoTrack(
        intent: Intent,
        callback: MediaProjection.Callback,
    ): LocalVideoTrack {
        checkNotDisposed()
        val videoCapturer = ScreenCapturerAndroid(intent, callback)
        return createLocalVideoTrack(videoCapturer)
    }

    /**
     * Creates a [LocalVideoTrack] backed by the provided [VideoCapturer].
     *
     * [VideoCapturer] will be managed by [LocalVideoTrack].
     *
     * @param videoCapturer a [VideoCapturer]
     * @return a [VideoCapturer]-backed [LocalVideoTrack]
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createLocalVideoTrack(videoCapturer: VideoCapturer): LocalVideoTrack {
        checkNotDisposed()
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        return WebRtcLocalVideoTrack(
            applicationContext = applicationContext,
            eglBase = eglBase,
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource),
            scope = CoroutineScope(),
            signalingDispatcher = signalingDispatcher,
        )
    }

    override fun createMediaConnection(config: MediaConnectionConfig): MediaConnection {
        checkNotDisposed()
        return WebRtcMediaConnection(
            factory = this,
            config = config,
            scope = CoroutineScope(),
            signalingDispatcher = signalingDispatcher,
        )
    }

    override fun dispose() {
        runBlocking { job.cancelAndJoin() }
        workerDispatcher.executor.maybeExecute {
            factory.dispose()
            audioDeviceModule.release()
        }
        workerDispatcher.close()
    }

    internal fun createPeerConnection(config: MediaConnectionConfig): PeerConnectionWrapper {
        val iceServers = config.iceServers.map {
            PeerConnection.IceServer.builder(it.urls.toList())
                .setUsername(it.username)
                .setPassword(it.password)
                .createIceServer()
        }
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            enableDscp = config.dscp
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = when (config.continualGathering) {
                true -> PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                else -> PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
            }
        }
        return PeerConnectionWrapper(factory, rtcConfig)
    }

    private fun createMediaTrackId() = UUID.randomUUID().toString()

    private fun CoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob(job) + workerDispatcher)

    private fun checkNotDisposed() =
        check(job.isActive) { "WebRtcMediaConnectionFactory has been disposed!" }

    private fun checkDeviceName(deviceName: String) =
        check(deviceName in getDeviceNames()) { "No available camera: $deviceName." }

    /**
     * A builder for [WebRtcMediaConnectionFactory].
     *
     * The created instance of [WebRtcMediaConnectionFactory] will use [DefaultVideoDecoderFactory],
     * [DefaultVideoEncoderFactory] and either [Camera2Enumerator] if the OS supports it or
     * [Camera1Enumerator] otherwise if corresponding methods were not used during before calling
     * [build].
     *
     * @param context an instance of application context
     */
    public class Builder(context: Context) {

        private val applicationContext = context.applicationContext

        private var eglBase: EglBase? = null
        private var cameraEnumerator: CameraEnumerator? = null
        private var audioDeviceModule: AudioDeviceModule? = null
        private var videoDecoderFactory: VideoDecoderFactory? = null
        private var videoEncoderFactory: VideoEncoderFactory? = null

        /**
         * Sets an [EglBase].
         *
         * @param eglBase a shared [EglBase] instance
         * @return this builder
         */
        public fun eglBase(eglBase: EglBase): Builder = apply {
            this.eglBase = eglBase
        }

        /**
         * Sets a [CameraEnumerator].
         *
         * @param cameraEnumerator an instance of [CameraEnumerator]
         * @return this builder
         */
        public fun cameraEnumerator(cameraEnumerator: CameraEnumerator): Builder = apply {
            this.cameraEnumerator = cameraEnumerator
        }

        /**
         * Sets an [AudioDeviceModule].
         *
         * @param audioDeviceModule an instance of [AudioDeviceModule]
         * @return this builder
         */
        public fun audioDeviceModule(audioDeviceModule: AudioDeviceModule): Builder = apply {
            this.audioDeviceModule = audioDeviceModule
        }

        /**
         * Sets a [VideoDecoderFactory].
         *
         * @param videoDecoderFactory an instance of [VideoDecoderFactory]
         * @return this builder
         */
        public fun videoDecoderFactory(videoDecoderFactory: VideoDecoderFactory): Builder = apply {
            this.videoDecoderFactory = videoDecoderFactory
        }

        /**
         * Sets a [VideoEncoderFactory].
         *
         * @param videoEncoderFactory an instance of [VideoEncoderFactory]
         * @return this builder
         */
        public fun videoEncoderFactory(videoEncoderFactory: VideoEncoderFactory): Builder = apply {
            this.videoEncoderFactory = videoEncoderFactory
        }

        /**
         * Builds [WebRtcMediaConnectionFactory].
         *
         * @return an instance of [WebRtcMediaConnectionFactory]
         */
        public fun build(): WebRtcMediaConnectionFactory = WebRtcMediaConnectionFactory(
            applicationContext = applicationContext,
            eglBase = eglBase,
            cameraEnumerator = cameraEnumerator ?: CameraEnumerator(applicationContext),
            audioDeviceModule = audioDeviceModule ?: JavaAudioDeviceModule(applicationContext),
            videoDecoderFactory = videoDecoderFactory ?: DefaultVideoDecoderFactory(eglBase),
            videoEncoderFactory = videoEncoderFactory ?: DefaultVideoEncoderFactory(eglBase),
        )
    }

    public companion object {

        private fun CameraEnumerator(context: Context): CameraEnumerator =
            when (Camera2Enumerator.isSupported(context.applicationContext)) {
                true -> Camera2Enumerator(context.applicationContext)
                else -> Camera1Enumerator()
            }

        private fun JavaAudioDeviceModule(context: Context): JavaAudioDeviceModule {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            return JavaAudioDeviceModule.builder(context.applicationContext)
                .setAudioAttributes(audioAttributes)
                .createAudioDeviceModule()
        }

        private fun DefaultVideoDecoderFactory(eglBase: EglBase?): DefaultVideoDecoderFactory =
            DefaultVideoDecoderFactory(eglBase?.eglBaseContext)

        private fun DefaultVideoEncoderFactory(eglBase: EglBase?): DefaultVideoEncoderFactory =
            DefaultVideoEncoderFactory(eglBase?.eglBaseContext, false, false)

        @JvmStatic
        public fun initialize(context: Context) {
            val applicationContext = context.applicationContext
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
        }
    }
}
