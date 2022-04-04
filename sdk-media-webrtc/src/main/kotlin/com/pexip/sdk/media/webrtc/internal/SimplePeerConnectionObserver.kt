package com.pexip.sdk.media.webrtc.internal

import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver

internal interface SimplePeerConnectionObserver : PeerConnection.Observer {

    override fun onAddStream(stream: MediaStream) {
    }

    override fun onRemoveStream(stream: MediaStream) {
    }

    override fun onIceCandidate(candidate: IceCandidate) {
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {
    }

    override fun onDataChannel(channel: DataChannel) {
    }

    override fun onIceConnectionReceivingChange(changing: Boolean) {
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
    }

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) {
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
    }

    override fun onTrack(transceiver: RtpTransceiver) {
    }
}
