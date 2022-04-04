package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import com.pexip.sdk.media.webrtc.coroutines.getMainLocalVideoTrack
import com.pexip.sdk.media.webrtc.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import org.webrtc.PeerConnection

class ConferenceWorkflow(private val service: InfinityService) :
    StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, ConferenceRendering>() {

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState {
        val conference = InfinityConference.create(
            service = service,
            node = props.node,
            conferenceAlias = props.conferenceAlias,
            response = props.response
        )
        val iceServer = PeerConnection.IceServer.builder(GoogleStunUrls).createIceServer()
        val connection = WebRtcMediaConnection.Builder(conference)
            .addIceServer(iceServer)
            .presentationInMix(true)
            .mainQualityProfile(QualityProfile.High)
            .build()
        return ConferenceState(
            conference = conference,
            connection = connection,
            sharedContext = connection.eglBaseContext
        )
    }

    override fun snapshotState(state: ConferenceState): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): ConferenceRendering {
        context.leaveSideEffect(renderState)
        context.mainLocalVideoTrackSideEffect(renderState.connection)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        return ConferenceRendering(
            sharedContext = renderState.sharedContext,
            localVideoTrack = renderState.localVideoTrack,
            remoteVideoTrack = renderState.remoteVideoTrack,
            onBackClick = context.send(::OnBackClick)
        )
    }

    private fun RenderContext.leaveSideEffect(renderState: ConferenceState) {
        runningSideEffect("${renderState.conference}Leave") {
            try {
                renderState.connection.sendMainAudio()
                renderState.connection.sendMainVideo()
                renderState.connection.startMainCapture()
                renderState.connection.start()
                awaitCancellation()
            } finally {
                renderState.connection.dispose()
                renderState.conference.leave()
            }
        }
    }

    private fun RenderContext.mainLocalVideoTrackSideEffect(connection: WebRtcMediaConnection) {
        runningSideEffect("${connection}mainLocalVideoTrack") {
            connection.getMainLocalVideoTrack()
                .map(::OnMainLocalVideoTrack)
                .collectLatest(actionSink::send)
        }
    }

    private fun RenderContext.mainRemoteVideoTrackSideEffect(connection: WebRtcMediaConnection) {
        runningSideEffect("${connection}mainRemoteVideoTrack") {
            connection.getMainRemoteVideoTrack()
                .map(::OnMainRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }
    }

    private companion object {

        private val GoogleStunUrls = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302"
        )
    }
}
