package com.pexip.sdk.media.webrtc

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.projection.MediaProjection
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.android.AndroidMediaConnectionFactory
import com.pexip.sdk.media.webrtc.internal.RealAudioHandler
import com.pexip.sdk.media.webrtc.internal.SimpleCameraEventsHandler
import com.pexip.sdk.media.webrtc.internal.WebRtcCameraVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcMediaConnection
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
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

public class WebRtcMediaConnectionFactory @JvmOverloads constructor(
    context: Context,
    private val eglBase: EglBase,
    private val cameraEnumerator: CameraEnumerator = when (Camera2Enumerator.isSupported(context)) {
        true -> Camera2Enumerator(context.applicationContext)
        else -> Camera1Enumerator()
    },
    videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext),
    videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(
        eglBase.eglBaseContext,
        false,
        false
    ),
) : AndroidMediaConnectionFactory {

    private val disposed = AtomicBoolean()
    private val applicationContext = context.applicationContext
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val audioHandler = RealAudioHandler(applicationContext, audioAttributes)
    private val audioDeviceModule = JavaAudioDeviceModule.builder(applicationContext)
        .setAudioAttributes(audioAttributes)
        .createAudioDeviceModule()
    private val factory = PeerConnectionFactory.builder()
        .setAudioDeviceModule(audioDeviceModule)
        .setVideoDecoderFactory(videoDecoderFactory)
        .setVideoEncoderFactory(videoEncoderFactory)
        .createPeerConnectionFactory()
    private val workerExecutor = Executors.newSingleThreadExecutor()
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private val signalingExecutor = Executors.newSingleThreadExecutor()

    override fun createLocalAudioTrack(): LocalAudioTrack {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack(createMediaTrackId(), audioSource)
        return WebRtcLocalAudioTrack(
            audioHandler = audioHandler,
            audioSource = audioSource,
            audioTrack = audioTrack,
            workerExecutor = workerExecutor,
            signalingExecutor = signalingExecutor
        )
    }

    override fun createCameraVideoTrack(): CameraVideoTrack {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        val deviceNames = cameraEnumerator.deviceNames
        val deviceName = deviceNames.firstOrNull(cameraEnumerator::isFrontFacing)
            ?: deviceNames.firstOrNull(cameraEnumerator::isBackFacing)
            ?: deviceNames.firstOrNull()
        return createCameraVideoTrack(checkNotNull(deviceName) { "No available camera." })
    }

    override fun createCameraVideoTrack(deviceName: String): CameraVideoTrack {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        check(deviceName in cameraEnumerator.deviceNames) { "No available camera: $deviceName." }
        val videoCapturer = cameraEnumerator.createCapturer(deviceName, null)
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        return WebRtcCameraVideoTrack(
            applicationContext = applicationContext,
            textureHelper = createSurfaceTextureHelper("CaptureThread:$deviceName"),
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource),
            workerExecutor = workerExecutor,
            signalingExecutor = signalingExecutor
        )
    }

    override fun createCameraVideoTrack(callback: CameraVideoTrack.Callback): CameraVideoTrack {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
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
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        check(deviceName in cameraEnumerator.deviceNames) { "No available camera: $deviceName." }
        val handler = SimpleCameraEventsHandler(callback, signalingExecutor)
        val videoCapturer = cameraEnumerator.createCapturer(deviceName, handler)
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        return WebRtcCameraVideoTrack(
            applicationContext = applicationContext,
            textureHelper = createSurfaceTextureHelper("CaptureThread:$deviceName"),
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource),
            workerExecutor = workerExecutor,
            signalingExecutor = signalingExecutor
        )
    }

    override fun createMediaProjectionVideoTrack(
        intent: Intent,
        callback: MediaProjection.Callback,
    ): LocalVideoTrack {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        val videoCapturer = ScreenCapturerAndroid(intent, callback)
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        return WebRtcLocalVideoTrack(
            applicationContext = applicationContext,
            textureHelper = createSurfaceTextureHelper("CaptureThread:MediaProjection"),
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource),
            workerExecutor = workerExecutor,
            signalingExecutor = signalingExecutor
        )
    }

    override fun createMediaConnection(config: MediaConnectionConfig): MediaConnection {
        check(!disposed.get()) { "WebRtcMediaConnectionFactory has been disposed!" }
        return WebRtcMediaConnection(
            factory = this,
            config = config,
            workerExecutor = workerExecutor,
            networkExecutor = networkExecutor,
            signalingExecutor = signalingExecutor
        )
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                factory.dispose()
                audioDeviceModule.release()
            }
            workerExecutor.shutdown()
            networkExecutor.shutdown()
            signalingExecutor.shutdown()
        } else {
            throw IllegalStateException("WebRtcMediaConnectionFactory has been disposed!")
        }
    }

    internal fun createPeerConnection(
        rtcConfig: PeerConnection.RTCConfiguration,
        observer: PeerConnection.Observer,
    ) = checkNotNull(factory.createPeerConnection(rtcConfig, observer))

    private fun createMediaTrackId() = UUID.randomUUID().toString()

    private fun createSurfaceTextureHelper(threadName: String) =
        SurfaceTextureHelper.create(threadName, eglBase.eglBaseContext)

    public companion object {

        @JvmStatic
        public fun initialize(context: Context) {
            val applicationContext = context.applicationContext
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
        }
    }
}
