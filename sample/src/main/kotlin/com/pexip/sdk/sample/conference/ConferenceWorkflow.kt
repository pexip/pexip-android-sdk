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
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.android.MediaProjectionVideoTrackFactory
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.media.coroutines.getPresentationRemoteVideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceProps
import com.pexip.sdk.sample.audio.AudioDeviceWorkflow
import com.pexip.sdk.sample.composer.ComposerWorkflow
import com.pexip.sdk.sample.dtmf.DtmfProps
import com.pexip.sdk.sample.dtmf.DtmfWorkflow
import com.pexip.sdk.sample.media.LocalMediaTrackProps
import com.pexip.sdk.sample.media.LocalMediaTrackWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
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
    private val mediaProjectionVideoTrackFactory: MediaProjectionVideoTrackFactory,
    private val audioDeviceWorkflow: AudioDeviceWorkflow,
    private val dtmfWorkflow: DtmfWorkflow,
    private val composerWorkflow: ComposerWorkflow,
    private val localMediaTrackWorkflow: LocalMediaTrackWorkflow,
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
        )
    }

    override fun snapshotState(state: ConferenceState): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): ConferenceRendering {
        val audioDeviceRendering = context.renderChild(
            child = audioDeviceWorkflow,
            props = AudioDeviceProps(renderState.audioDevicesVisible),
            handler = ::OnAudioDeviceOutput
        )
        context.bindConferenceServiceSideEffect()
        context.leaveSideEffect(renderProps, renderState)
        context.screenCapturingSideEffect(renderState.screenCaptureVideoTrack)
        context.screenCaptureVideoTrackSideEffect(renderState.screenCaptureData)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        context.conferenceEventsSideEffect(renderState.conference)
        context.presentationRemoteVideoTrackSideEffect(renderState.connection)
        return when (renderState.showingConferenceEvents) {
            true -> ConferenceEventsRendering(
                conferenceEvents = renderState.conferenceEvents,
                composerRendering = context.renderChild(
                    child = composerWorkflow,
                    handler = ::OnComposerOutput
                ),
                onBackClick = context.send(::OnBackClick)
            )
            else -> ConferenceCallRendering(
                cameraVideoTrack = renderProps.cameraVideoTrack,
                mainRemoteVideoTrack = renderState.mainRemoteVideoTrack,
                presentationRemoteVideoTrack = renderState.presentationRemoteVideoTrack,
                audioDeviceRendering = audioDeviceRendering,
                dtmfRendering = context.renderChild(
                    child = dtmfWorkflow,
                    props = DtmfProps(renderState.dtmfVisible),
                    handler = ::OnDtmfOutput
                ),
                cameraVideoTrackRendering = when (renderProps.cameraVideoTrack) {
                    null -> null
                    else -> context.renderChild(
                        child = localMediaTrackWorkflow,
                        key = "cameraVideoTrack",
                        props = LocalMediaTrackProps(renderProps.cameraVideoTrack)
                    )
                },
                microphoneAudioTrackRendering = when (renderProps.microphoneAudioTrack) {
                    null -> null
                    else -> context.renderChild(
                        child = localMediaTrackWorkflow,
                        key = "microphoneAudioTrack",
                        props = LocalMediaTrackProps(renderProps.microphoneAudioTrack)
                    )
                },
                screenCapturing = renderState.screenCapturing,
                onScreenCapture = context.send(::OnScreenCapture),
                onAudioDevicesChange = context.send(::OnAudioDevicesChange),
                onDtmfChange = context.send(::OnDtmfChange),
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

    private fun RenderContext.leaveSideEffect(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
    ) = runningSideEffect("${renderState.conference}Leave") {
        try {
            renderState.connection.setMainVideoTrack(renderProps.cameraVideoTrack)
            renderState.connection.setMainAudioTrack(renderProps.microphoneAudioTrack)
            renderState.connection.setMainRemoteAudioTrackEnabled(true)
            renderState.connection.setMainRemoteVideoTrackEnabled(true)
            renderState.connection.start()
            awaitCancellation()
        } finally {
            renderState.connection.dispose()
            renderState.conference.leave()
            renderState.screenCaptureVideoTrack?.dispose()
        }
    }

    private fun RenderContext.screenCapturingSideEffect(track: LocalVideoTrack?) {
        if (track != null) runningSideEffect("${track}Capturing") {
            track.getCapturing()
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
