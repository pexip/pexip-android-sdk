package com.pexip.sdk.video.internal

import android.content.Context
import com.pexip.sdk.video.VideoTrack
import com.pexip.sdk.video.VideoTrackListener
import org.webrtc.CameraEnumerator
import org.webrtc.EglBase
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.RtpTransceiver.RtpTransceiverInit
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import java.util.concurrent.CopyOnWriteArraySet
import org.webrtc.VideoTrack as WebRtcVideoTrack

internal interface VideoStrategy : MediaStrategy {

    fun startCapture() {
        // noop
    }

    fun stopCapture() {
        // noop
    }

    fun registerLocalVideoTrackListener(listener: VideoTrackListener) {
        // noop
    }

    fun unregisterLocalVideoTrackListener(listener: VideoTrackListener) {
        // noop
    }

    fun registerRemoteVideoTrackListener(listener: VideoTrackListener) {
        // noop
    }

    fun unregisterRemoteVideoTrackListener(listener: VideoTrackListener) {
        // noop
    }

    fun onTrack(transceiver: RtpTransceiver) {
        // noop
    }
}

internal fun PeerConnectionFactory.createVideoStrategy(
    direction: RtpTransceiverDirection,
    enumerator: CameraEnumerator,
    sharedContext: EglBase.Context,
    applicationContext: Context,
) = when (direction) {
    RtpTransceiverDirection.SEND_RECV, RtpTransceiverDirection.SEND_ONLY -> {
        val deviceNames = enumerator.deviceNames
        val deviceName = deviceNames.firstOrNull(enumerator::isFrontFacing)
            ?: deviceNames.firstOrNull(enumerator::isBackFacing)
            ?: deviceNames.first()
        val capturer = enumerator.createCapturer(deviceName, null)
        val source = createVideoSource(capturer.isScreencast)
        val threadName = "${capturer.javaClass.name}Thread"
        SendVideoStrategy(
            direction = direction,
            capturer = capturer,
            source = source,
            helper = SurfaceTextureHelper.create(threadName, sharedContext),
            track = createVideoTrack("ARDAMSv0", source),
            sharedContext = sharedContext,
            applicationContext = applicationContext
        )
    }
    RtpTransceiverDirection.RECV_ONLY -> RecvOnlyVideoStrategy
    RtpTransceiverDirection.INACTIVE -> InactiveVideoStrategy
}

private class SendVideoStrategy(
    private val direction: RtpTransceiverDirection,
    private val capturer: VideoCapturer,
    private val source: VideoSource,
    private val helper: SurfaceTextureHelper,
    private val track: WebRtcVideoTrack,
    private val sharedContext: EglBase.Context,
    private val applicationContext: Context,
) : VideoStrategy {

    private lateinit var transceiver: RtpTransceiver

    private val localVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()
    private val remoteVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()

    override fun init(connection: PeerConnection) {
        capturer.initialize(helper, applicationContext, source.capturerObserver)
        transceiver = connection.addTransceiver(track, RtpTransceiverInit(direction))
    }

    override fun startCapture() {
        capturer.startCapture(1280, 720, 25)
    }

    override fun stopCapture() {
        capturer.stopCapture()
    }

    override fun dispose() {
        track.dispose()
        source.dispose()
        capturer.dispose()
        helper.dispose()
    }

    override fun registerLocalVideoTrackListener(listener: VideoTrackListener) {
        val sender = transceiver.sender
        val videoTrack = sender?.track() as? WebRtcVideoTrack
        val vt = videoTrack?.let { VideoTrack(sharedContext, videoTrack) }
        listener.onVideoTrack(vt)
        localVideoTrackListeners += listener
    }

    override fun unregisterLocalVideoTrackListener(listener: VideoTrackListener) {
        localVideoTrackListeners -= listener
    }

    override fun registerRemoteVideoTrackListener(listener: VideoTrackListener) {
        val receiver = transceiver.receiver
        val videoTrack = receiver?.track() as? WebRtcVideoTrack
        val vt = videoTrack?.let { VideoTrack(sharedContext, videoTrack) }
        listener.onVideoTrack(vt)
        remoteVideoTrackListeners += listener
    }

    override fun unregisterRemoteVideoTrackListener(listener: VideoTrackListener) {
        remoteVideoTrackListeners -= listener
    }

    override fun onTrack(transceiver: RtpTransceiver) {
        val receiver = transceiver
            .takeIf { it.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO }
            ?.receiver ?: return
        val videoTrack = receiver.track() as? WebRtcVideoTrack
        val vt = videoTrack?.let { VideoTrack(sharedContext, it) }
        remoteVideoTrackListeners.forEach { it.onVideoTrack(vt) }
    }
}

private object RecvOnlyVideoStrategy : VideoStrategy {

    override fun init(connection: PeerConnection) {
        connection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
            RtpTransceiverInit(RtpTransceiverDirection.RECV_ONLY)
        )
    }
}

internal object InactiveVideoStrategy : VideoStrategy
