package com.pexip.sdk.video.conference.internal

import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection

internal sealed interface AudioStrategy : MediaStrategy

internal fun PeerConnectionFactory.createAudioStrategy(direction: RtpTransceiverDirection) =
    when (direction) {
        RtpTransceiverDirection.SEND_RECV, RtpTransceiverDirection.SEND_ONLY -> {
            val constrains = MediaConstraints()
            val source = createAudioSource(constrains)
            val track = createAudioTrack("ARDAMSa0", source)
            SendAudioStrategy(direction, source, track)
        }
        RtpTransceiverDirection.RECV_ONLY -> RecvOnlyAudioStrategy
        RtpTransceiverDirection.INACTIVE -> InactiveAudioStrategy
    }

private class SendAudioStrategy(
    private val direction: RtpTransceiverDirection,
    private val source: AudioSource,
    private val track: AudioTrack,
) : AudioStrategy {

    override fun init(connection: PeerConnection) {
        connection.addTransceiver(track, RtpTransceiver.RtpTransceiverInit(direction))
    }

    override fun dispose() {
        track.dispose()
        source.dispose()
    }
}

private object RecvOnlyAudioStrategy : AudioStrategy {

    override fun init(connection: PeerConnection) {
        connection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.RECV_ONLY)
        )
    }
}

internal object InactiveAudioStrategy : AudioStrategy
