package com.pexip.sdk.sample.conference

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.coroutines.getConferenceEvents
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.media.coroutines.getPresentationRemoteVideoTrack
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

class ConferenceWorkflow(
    private val service: InfinityService,
    private val factory: MediaConnectionFactory,
) : StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, ConferenceRendering>() {

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState {
        val conference = InfinityConference.create(
            service = service,
            node = props.node,
            conferenceAlias = props.conferenceAlias,
            response = props.response
        )
        val iceServer = IceServer.Builder(GoogleStunUrls).build()
        val config = MediaConnectionConfig.Builder(conference)
            .addIceServer(iceServer)
            .presentationInMain(props.presentationInMain)
            .build()
        return ConferenceState(
            conference = conference,
            connection = factory.createMediaConnection(config),
            localAudioTrack = factory.createLocalAudioTrack(),
            cameraVideoTrack = factory.createCameraVideoTrack()
        )
    }

    override fun snapshotState(state: ConferenceState): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): ConferenceRendering {
        context.leaveSideEffect(renderState)
        context.localAudioCapturingSideEffect(renderState.localAudioTrack)
        context.cameraCapturingSideEffect(renderState.cameraVideoTrack)
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
                localAudioCapturing = renderState.localAudioCapturing,
                cameraCapturing = renderState.cameraCapturing,
                cameraVideoTrack = renderState.cameraVideoTrack,
                mainRemoteVideoTrack = renderState.mainRemoteVideoTrack,
                presentationRemoteVideoTrack = renderState.presentationRemoteVideoTrack,
                onToggleLocalAudioCapturing = context.send(::OnToggleLocalAudioCapturing),
                onToggleCameraCapturing = context.send(::OnToggleCameraCapturing),
                onConferenceEventsClick = context.send(::OnConferenceEventsClick),
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.leaveSideEffect(renderState: ConferenceState) =
        runningSideEffect("${renderState.conference}Leave") {
            try {
                renderState.localAudioTrack.startCapture()
                renderState.cameraVideoTrack.startCapture()
                renderState.connection.sendMainAudio(renderState.localAudioTrack)
                renderState.connection.sendMainVideo(renderState.cameraVideoTrack)
                renderState.connection.start()
                awaitCancellation()
            } finally {
                renderState.connection.dispose()
                renderState.conference.leave()
                renderState.localAudioTrack.dispose()
                renderState.cameraVideoTrack.dispose()
            }
        }

    private fun RenderContext.localAudioCapturingSideEffect(localAudioTrack: LocalAudioTrack) =
        runningSideEffect("${localAudioTrack}Capturing") {
            localAudioTrack.getCapturing()
                .map(::OnMicrophoneCapturing)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.cameraCapturingSideEffect(cameraVideoTrack: CameraVideoTrack) =
        runningSideEffect("${cameraVideoTrack}Capturing") {
            cameraVideoTrack.getCapturing()
                .map(::OnCameraCapturing)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.mainRemoteVideoTrackSideEffect(connection: MediaConnection) =
        runningSideEffect("${connection}MainRemoteVideoTrack") {
            connection.getMainRemoteVideoTrack()
                .map(::OnMainRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.presentationRemoteVideoTrackSideEffect(connection: MediaConnection) =
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
