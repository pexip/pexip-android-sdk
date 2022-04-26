package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.coroutines.getConferenceEvents
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.coroutines.getMainCapturing
import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import com.pexip.sdk.media.webrtc.coroutines.getMainLocalVideoTrack
import com.pexip.sdk.media.webrtc.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.media.webrtc.coroutines.getPresentationRemoteVideoTrack
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
            .presentationInMain(props.presentationInMain)
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
        context.mainVideoCapturingSideEffect(renderState.connection)
        context.mainLocalVideoTrackSideEffect(renderState.connection)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        context.conferenceEventsSideEffect(renderState.conference)
        context.receivePresentationSideEffect(renderState)
        context.presentationRemoteVideoTrackSideEffect(renderState.connection)
        return when (renderState.showingConferenceEvents) {
            true -> ConferenceEventsRendering(
                conferenceEvents = renderState.conferenceEvents,
                message = renderState.message,
                onMessageChange = context.send(::OnMessageChange),
                submitEnabled = renderState.submitEnabled,
                onSubmitClick = context.send(::OnSubmitClick),
                onBackClick = context.send(::OnBackClick)
            )
            else -> ConferenceCallRendering(
                sharedContext = renderState.sharedContext,
                mainCapturing = renderState.mainCapturing,
                mainLocalVideoTrack = renderState.mainLocalVideoTrack,
                mainRemoteVideoTrack = renderState.mainRemoteVideoTrack,
                presentationRemoteVideoTrack = renderState.presentationRemoteVideoTrack,
                onToggleMainCapturing = context.send(::OnToggleMainVideoCapturing),
                onConferenceEventsClick = context.send(::OnConferenceEventsClick),
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.leaveSideEffect(renderState: ConferenceState) =
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

    private fun RenderContext.mainLocalVideoTrackSideEffect(connection: WebRtcMediaConnection) =
        runningSideEffect("${connection}MainLocalVideoTrack") {
            connection.getMainLocalVideoTrack()
                .map(::OnMainLocalVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.mainRemoteVideoTrackSideEffect(connection: WebRtcMediaConnection) =
        runningSideEffect("${connection}MainRemoteVideoTrack") {
            connection.getMainRemoteVideoTrack()
                .map(::OnMainRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.mainVideoCapturingSideEffect(connection: WebRtcMediaConnection) =
        runningSideEffect("${connection}MainCapturing") {
            connection.getMainCapturing()
                .map(::OnMainCapturing)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.presentationRemoteVideoTrackSideEffect(connection: WebRtcMediaConnection) =
        runningSideEffect("${connection}PresentationRemoteVideoTrack") {
            connection.getPresentationRemoteVideoTrack()
                .map(::OnPresentationRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.conferenceEventsSideEffect(conference: Conference) =
        runningSideEffect("${conference}ConferenceEvents") {
            conference.getConferenceEvents()
                .map(::OnConferenceEvent)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.receivePresentationSideEffect(renderState: ConferenceState) =
        runningSideEffect("presentation: ${renderState.presentation},${renderState.connection}") {
            if (renderState.presentation) {
                renderState.connection.startPresentationReceive()
            } else {
                renderState.connection.stopPresentationReceive()
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
