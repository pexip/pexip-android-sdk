/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.sample.conference

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.os.IBinder
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import com.pexip.sdk.conference.coroutines.getConferenceEvents
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
import com.pexip.sdk.sample.bandwidth.BandwidthProps
import com.pexip.sdk.sample.bandwidth.BandwidthWorkflow
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConferenceWorkflow @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val mediaConnectionFactory: MediaConnectionFactory,
    private val mediaProjectionVideoTrackFactory: MediaProjectionVideoTrackFactory,
    private val audioDeviceWorkflow: AudioDeviceWorkflow,
    private val bandwidthWorkflow: BandwidthWorkflow,
    private val dtmfWorkflow: DtmfWorkflow,
    private val composerWorkflow: ComposerWorkflow,
    private val localMediaTrackWorkflow: LocalMediaTrackWorkflow,
) : StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, ConferenceRendering>() {

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState {
        val iceServer = IceServer.Builder(GoogleStunUrls).build()
        val config = MediaConnectionConfig.Builder(props.conference.signaling)
            .addIceServer(iceServer)
            .presentationInMain(props.presentationInMain)
            .build()
        return ConferenceState(connection = mediaConnectionFactory.createMediaConnection(config))
    }

    override fun onPropsChanged(
        old: ConferenceProps,
        new: ConferenceProps,
        state: ConferenceState,
    ): ConferenceState {
        if (old.conference == new.conference) return state
        return initialState(new, null)
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
            handler = ::OnAudioDeviceOutput,
        )
        context.bindConferenceServiceSideEffect()
        context.leaveSideEffect(renderProps, renderState)
        context.screenCapturingSideEffect(renderState.screenCaptureVideoTrack)
        context.screenCaptureVideoTrackSideEffect(renderState.screenCaptureData)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        context.conferenceEventsSideEffect(renderProps)
        context.presentationRemoteVideoTrackSideEffect(renderState.connection)
        return when (renderState.showingConferenceEvents) {
            true -> ConferenceEventsRendering(
                conferenceEvents = renderState.conferenceEvents,
                composerRendering = context.renderChild(
                    child = composerWorkflow,
                    handler = ::OnComposerOutput,
                ),
                onBackClick = context.send(::OnBackClick),
            )
            else -> ConferenceCallRendering(
                cameraVideoTrack = renderProps.cameraVideoTrack,
                mainRemoteVideoTrack = renderState.mainRemoteVideoTrack,
                presentationRemoteVideoTrack = renderState.presentationRemoteVideoTrack,
                audioDeviceRendering = audioDeviceRendering,
                bandwidthRendering = context.renderChild(
                    child = bandwidthWorkflow,
                    props = BandwidthProps(renderState.bandwidthVisible),
                    handler = ::OnBandwidthOutput,
                ),
                dtmfRendering = context.renderChild(
                    child = dtmfWorkflow,
                    props = DtmfProps(renderState.dtmfVisible),
                    handler = ::OnDtmfOutput,
                ),
                cameraVideoTrackRendering = when (renderProps.cameraVideoTrack) {
                    null -> null
                    else -> context.renderChild(
                        child = localMediaTrackWorkflow,
                        key = "cameraVideoTrack",
                        props = LocalMediaTrackProps(renderProps.cameraVideoTrack),
                    )
                },
                microphoneAudioTrackRendering = when (renderProps.microphoneAudioTrack) {
                    null -> null
                    else -> context.renderChild(
                        child = localMediaTrackWorkflow,
                        key = "microphoneAudioTrack",
                        props = LocalMediaTrackProps(renderProps.microphoneAudioTrack),
                    )
                },
                screenCapturing = renderState.screenCapturing,
                onScreenCapture = context.send(::OnScreenCapture),
                onAudioDevicesChange = context.send(::OnAudioDevicesChange),
                onBandwidthChange = context.send(::OnBandwidthChange),
                onDtmfChange = context.send(::OnDtmfChange),
                onStopScreenCapture = context.send(::OnStopScreenCapture),
                onConferenceEventsClick = context.send(::OnConferenceEventsClick),
                onBackClick = context.send(::OnBackClick),
            )
        }
    }

    private fun RenderContext.bindConferenceServiceSideEffect() =
        runningSideEffect("bindConferenceServiceSideEffect()") {
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
    ) = runningSideEffect("leaveSideEffect(${renderProps.conference})") {
        try {
            renderState.connection.setMainVideoTrack(renderProps.cameraVideoTrack)
            renderState.connection.setMainAudioTrack(renderProps.microphoneAudioTrack)
            renderState.connection.setMainRemoteAudioTrackEnabled(true)
            renderState.connection.setMainRemoteVideoTrackEnabled(true)
            renderState.connection.start()
            awaitCancellation()
        } finally {
            renderState.connection.dispose()
            renderProps.conference.leave()
            renderState.screenCaptureVideoTrack?.dispose()
        }
    }

    private fun RenderContext.screenCapturingSideEffect(track: LocalVideoTrack?) {
        if (track != null) {
            runningSideEffect("screenCapturingSideEffect($track)") {
                track.getCapturing()
                    .map(::OnScreenCapturing)
                    .collectLatest(actionSink::send)
            }
        }
    }

    private fun RenderContext.screenCaptureVideoTrackSideEffect(data: Intent?) {
        if (data != null) {
            runningSideEffect("screenCaptureVideoTrackSideEffect($data)") {
                val callback = object : MediaProjection.Callback() {
                    override fun onStop() {
                        actionSink.send(OnStopScreenCapture())
                    }
                }
                val localVideoTrack =
                    mediaProjectionVideoTrackFactory.createMediaProjectionVideoTrack(
                        intent = data,
                        callback = callback,
                    )
                actionSink.send(OnScreenCaptureVideoTrack(localVideoTrack))
            }
        }
    }

    private fun RenderContext.mainRemoteVideoTrackSideEffect(connection: MediaConnection) =
        runningSideEffect("mainRemoteVideoTrackSideEffect($connection)") {
            connection.getMainRemoteVideoTrack()
                .map(::OnMainRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.presentationRemoteVideoTrackSideEffect(connection: MediaConnection) =
        runningSideEffect("presentationRemoteVideoTrackSideEffect($connection)") {
            connection.getPresentationRemoteVideoTrack()
                .map(::OnPresentationRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.conferenceEventsSideEffect(renderProps: ConferenceProps) {
        val conference = renderProps.conference
        runningSideEffect("conferenceEventsSideEffect($conference)") {
            val events = conference.getConferenceEvents().shareIn(this, SharingStarted.Lazily)
            events
                .map(::toConferenceAction)
                .onEach(actionSink::send)
                .launchIn(this)
            events.filterIsInstance<ReferConferenceEvent>()
                .map {
                    val c = conference.referer.refer(it)
                    OnReferConferenceEvent(c)
                }
                .onEach(actionSink::send)
                .launchIn(this)
        }
    }

    private fun toConferenceAction(event: ConferenceEvent) = when (event) {
        is PresentationStartConferenceEvent -> OnPresentationStartConferenceEvent(event)
        is PresentationStopConferenceEvent -> OnPresentationStopConferenceEvent(event)
        is DisconnectConferenceEvent -> OnDisconnectConferenceEvent(event)
        is FailureConferenceEvent -> OnFailureConferenceEvent(event)
        else -> OnConferenceEvent(event)
    }

    private companion object {

        private val GoogleStunUrls = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302",
        )
    }
}
