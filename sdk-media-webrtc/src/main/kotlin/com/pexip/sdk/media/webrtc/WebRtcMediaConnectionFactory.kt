package com.pexip.sdk.media.webrtc

import android.content.Context
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.webrtc.internal.WebRtcCameraVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcMediaConnection
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.ContextUtils
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import java.util.UUID
import java.util.concurrent.Executors

public class WebRtcMediaConnectionFactory @JvmOverloads constructor(
    context: Context,
    private val eglBase: EglBase,
    videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext),
    videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(
        eglBase.eglBaseContext,
        false,
        false
    ),
) : MediaConnectionFactory {

    @Deprecated(
        message = "Use primary constructor instead.",
        level = DeprecationLevel.ERROR
    )
    public constructor() : this(ContextUtils.getApplicationContext(), EglBase.create())

    private val factory = PeerConnectionFactory.builder()
        .setVideoDecoderFactory(videoDecoderFactory)
        .setVideoEncoderFactory(videoEncoderFactory)
        .createPeerConnectionFactory()
    private val applicationContext = context.applicationContext
    private val cameraEnumerator = when (Camera2Enumerator.isSupported(applicationContext)) {
        true -> Camera2Enumerator(applicationContext)
        else -> Camera1Enumerator()
    }

    @Deprecated(message = "Use EglBase.eglBaseContext instead.", level = DeprecationLevel.ERROR)
    public val eglBaseContext: EglBase.Context
        get() = throw UnsupportedOperationException("Deprecated.")

    override fun createLocalAudioTrack(): LocalAudioTrack {
        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack(createMediaTrackId(), audioSource)
        return WebRtcLocalAudioTrack(audioSource, audioTrack)
    }

    override fun createCameraVideoTrack(): CameraVideoTrack {
        val deviceNames = cameraEnumerator.deviceNames
        val deviceName = deviceNames.firstOrNull(cameraEnumerator::isFrontFacing)
            ?: deviceNames.firstOrNull(cameraEnumerator::isBackFacing)
            ?: deviceNames.first()
        return createCameraVideoTrack(deviceName)
    }

    override fun createCameraVideoTrack(deviceName: String): CameraVideoTrack {
        require(deviceName in cameraEnumerator.deviceNames) { "Unknown device name: $deviceName." }
        val textureHelper =
            SurfaceTextureHelper.create("CaptureThread:$deviceName", eglBase.eglBaseContext)
        val videoCapturer = cameraEnumerator.createCapturer(deviceName, null)
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        val videoTrack = factory.createVideoTrack(createMediaTrackId(), videoSource)
        return WebRtcCameraVideoTrack(
            applicationContext = applicationContext,
            textureHelper = textureHelper,
            videoCapturer = videoCapturer,
            videoSource = videoSource,
            videoTrack = videoTrack
        )
    }

    override fun createMediaConnection(config: MediaConnectionConfig): MediaConnection =
        WebRtcMediaConnection(
            factory = this,
            config = config,
            workerExecutor = Executors.newSingleThreadExecutor(),
            signalingExecutor = Executors.newSingleThreadExecutor()
        )

    override fun dispose() {
        factory.dispose()
    }

    internal fun createPeerConnection(
        rtcConfig: PeerConnection.RTCConfiguration,
        observer: PeerConnection.Observer,
    ) = checkNotNull(factory.createPeerConnection(rtcConfig, observer))

    private fun createMediaTrackId() = UUID.randomUUID().toString()

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
