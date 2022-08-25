package com.pexip.sdk.sample.conference

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.os.IBinder
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.coroutines.getConferenceEvents
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.CameraVideoTrackFactory
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalAudioTrackFactory
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.android.MediaProjectionVideoTrackFactory
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.media.coroutines.getPresentationRemoteVideoTrack
import com.pexip.sdk.sample.dtmf.DtmfProps
import com.pexip.sdk.sample.dtmf.DtmfWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConferenceWorkflow @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val service: InfinityService,
    private val mediaConnectionFactory: MediaConnectionFactory,
    private val localAudioTrackFactory: LocalAudioTrackFactory,
    private val cameraVideoTrackFactory: CameraVideoTrackFactory,
    private val mediaProjectionVideoTrackFactory: MediaProjectionVideoTrackFactory,
    private val dtmfWorkflow: DtmfWorkflow,
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
            connection = mediaConnectionFactory.createMediaConnection(config),
            localAudioTrack = localAudioTrackFactory.createLocalAudioTrack(),
            cameraVideoTrack = cameraVideoTrackFactory.createCameraVideoTrack()
        )
    }

    override fun snapshotState(state: ConferenceState): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): ConferenceRendering {
        context.bindConferenceServiceSideEffect()
        context.leaveSideEffect(renderState)
        context.localAudioCapturingSideEffect(renderState.localAudioTrack)
        context.cameraCapturingSideEffect(renderState.cameraVideoTrack)
        context.screenCapturingSideEffect(renderState.screenCaptureVideoTrack)
        context.screenCaptureVideoTrackSideEffect(renderState.screenCaptureData)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        context.conferenceEventsSideEffect(renderState.conference)
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
                dtmfRendering = when (renderState.showingDtmf) {
                    true -> context.renderChild(
                        child = dtmfWorkflow,
                        props = DtmfProps(renderState.connection),
                        handler = ::OnDtmfOutput
                    )
                    else -> null
                },
                screenCapturing = renderState.screenCapturing,
                onScreenCapture = context.send(::OnScreenCapture),
                onToggleDtmfClick = context.send(::OnToggleDtmf),
                onToggleLocalAudioCapturing = context.send(::OnToggleLocalAudioCapturing),
                onToggleCameraCapturing = context.send(::OnToggleCameraCapturing),
                onStopScreenCapture = context.send(::OnStopScreenCapture),
                onConferenceEventsClick = context.send(::OnConferenceEventsClick),
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.bindConferenceServiceSideEffect() =
        runningSideEffect("bindConferenceService") {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    // noop
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    // noop
                }
            }
            try {
                val intent = Intent(applicationContext, ConferenceService::class.java)
                applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                awaitCancellation()
            } finally {
                applicationContext.unbindService(connection)
            }
        }

    private fun RenderContext.leaveSideEffect(renderState: ConferenceState) =
        runningSideEffect("${renderState.conference}Leave") {
            try {
                renderState.localAudioTrack.startCapture()
                renderState.cameraVideoTrack.startCapture()
                renderState.connection.setMainAudioTrack(renderState.localAudioTrack)
                renderState.connection.setMainVideoTrack(renderState.cameraVideoTrack)
                renderState.connection.start()
                awaitCancellation()
            } finally {
                renderState.connection.dispose()
                renderState.conference.leave()
                renderState.localAudioTrack.dispose()
                renderState.cameraVideoTrack.dispose()
                renderState.screenCaptureVideoTrack?.dispose()
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

    private fun RenderContext.screenCapturingSideEffect(localVideoTrack: LocalVideoTrack?) {
        if (localVideoTrack != null) runningSideEffect("${localVideoTrack}Capturing") {
            localVideoTrack.getCapturing()
                .map(::OnScreenCapturing)
                .collectLatest(actionSink::send)
        }
    }

    private fun RenderContext.screenCaptureVideoTrackSideEffect(data: Intent?) {
        if (data != null) runningSideEffect("${data}Data") {
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    actionSink.send(OnStopScreenCapture())
                }
            }
            val localVideoTrack = mediaProjectionVideoTrackFactory.createMediaProjectionVideoTrack(
                intent = data,
                callback = callback
            )
            actionSink.send(OnScreenCaptureVideoTrack(localVideoTrack))
        }
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
