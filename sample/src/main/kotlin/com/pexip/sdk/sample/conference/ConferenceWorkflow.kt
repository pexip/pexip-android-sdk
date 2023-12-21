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
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.conference.coroutines.getConferenceEvents
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.android.MediaProjectionVideoTrackFactory
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.coroutines.getMainRemoteVideoTrack
import com.pexip.sdk.media.coroutines.getPresentationRemoteVideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceOutput
import com.pexip.sdk.sample.audio.AudioDeviceProps
import com.pexip.sdk.sample.audio.AudioDeviceWorkflow
import com.pexip.sdk.sample.bandwidth.BandwidthOutput
import com.pexip.sdk.sample.bandwidth.BandwidthProps
import com.pexip.sdk.sample.bandwidth.BandwidthWorkflow
import com.pexip.sdk.sample.bandwidth.bitrate
import com.pexip.sdk.sample.chat.ChatOutput
import com.pexip.sdk.sample.chat.ChatProps
import com.pexip.sdk.sample.chat.ChatWorkflow
import com.pexip.sdk.sample.dtmf.DtmfOutput
import com.pexip.sdk.sample.dtmf.DtmfProps
import com.pexip.sdk.sample.dtmf.DtmfWorkflow
import com.pexip.sdk.sample.media.LocalMediaTrackProps
import com.pexip.sdk.sample.media.LocalMediaTrackWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    private val chatWorkflow: ChatWorkflow,
    private val dtmfWorkflow: DtmfWorkflow,
    private val localMediaTrackWorkflow: LocalMediaTrackWorkflow,
) : StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, Any>() {

    private val googleStunUrls = listOf(
        "stun:stun.l.google.com:19302",
        "stun:stun1.l.google.com:19302",
        "stun:stun2.l.google.com:19302",
        "stun:stun3.l.google.com:19302",
        "stun:stun4.l.google.com:19302",
    )

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState {
        val iceServer = IceServer.Builder(googleStunUrls).build()
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
    ): Any {
        val audioDeviceRendering = context.renderChild(
            child = audioDeviceWorkflow,
            props = AudioDeviceProps(renderState.audioDevicesVisible),
            handler = ::onAudioDeviceOutput,
        )
        val chatRendering = context.renderChild(
            child = chatWorkflow,
            props = ChatProps(renderProps.conference.messenger),
            handler = ::onChatOutput,
        )
        context.bindConferenceServiceSideEffect()
        context.leaveSideEffect(renderProps, renderState)
        context.splashScreenSideEffect(renderProps)
        context.screenCapturingSideEffect(renderState.screenCaptureVideoTrack)
        context.screenCaptureVideoTrackSideEffect(renderState.screenCaptureData)
        context.mainRemoteVideoTrackSideEffect(renderState.connection)
        context.conferenceEventsSideEffect(renderProps)
        context.presentationRemoteVideoTrackSideEffect(renderState.connection)
        context.aspectRatioSideEffect(renderState)
        return when (renderState.showingChat) {
            true -> chatRendering
            else -> ConferenceRendering(
                splashScreen = renderState.splashScreen,
                cameraVideoTrack = renderProps.cameraVideoTrack,
                mainRemoteVideoTrack = renderState.mainRemoteVideoTrack,
                presentationRemoteVideoTrack = renderState.presentationRemoteVideoTrack,
                audioDeviceRendering = audioDeviceRendering,
                bandwidthRendering = context.renderChild(
                    child = bandwidthWorkflow,
                    props = BandwidthProps(renderState.bandwidthVisible),
                    handler = ::onBandwidthOutput,
                ),
                dtmfRendering = context.renderChild(
                    child = dtmfWorkflow,
                    props = DtmfProps(renderState.dtmfVisible),
                    handler = ::onDtmfOutput,
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
                onScreenCapture = context.send(::onScreenCapture),
                onAspectRatioChange = context.send(::onAspectRatioChange),
                onAudioDevicesChange = context.send(::onAudioDevicesChange),
                onBandwidthChange = context.send(::onBandwidthChange),
                onDtmfChange = context.send(::onDtmfChange),
                onStopScreenCapture = context.send(::onStopScreenCapture),
                onChatClick = context.send(::onChatClick),
                onBackClick = context.send(::onBackClick),
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

    private fun RenderContext.splashScreenSideEffect(renderProps: ConferenceProps) {
        val conference = renderProps.conference
        runningSideEffect("splashScreenSideEffect($conference)") {
            conference.theme.splashScreen
                .map(::onSplashScreen)
                .collect(actionSink::send)
        }
    }

    private fun RenderContext.screenCapturingSideEffect(track: LocalVideoTrack?) {
        if (track != null) {
            runningSideEffect("screenCapturingSideEffect($track)") {
                track.getCapturing()
                    .map(::onScreenCapturing)
                    .collectLatest(actionSink::send)
            }
        }
    }

    private fun RenderContext.screenCaptureVideoTrackSideEffect(data: Intent?) {
        if (data != null) {
            runningSideEffect("screenCaptureVideoTrackSideEffect($data)") {
                val callback = object : MediaProjection.Callback() {
                    override fun onStop() {
                        actionSink.send(onStopScreenCapture())
                    }
                }
                val localVideoTrack =
                    mediaProjectionVideoTrackFactory.createMediaProjectionVideoTrack(
                        intent = data,
                        callback = callback,
                    )
                actionSink.send(onScreenCaptureVideoTrack(localVideoTrack))
            }
        }
    }

    private fun RenderContext.mainRemoteVideoTrackSideEffect(connection: MediaConnection) =
        runningSideEffect("mainRemoteVideoTrackSideEffect($connection)") {
            connection.getMainRemoteVideoTrack()
                .map(::onMainRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.presentationRemoteVideoTrackSideEffect(connection: MediaConnection) =
        runningSideEffect("presentationRemoteVideoTrackSideEffect($connection)") {
            connection.getPresentationRemoteVideoTrack()
                .map(::onPresentationRemoteVideoTrack)
                .collectLatest(actionSink::send)
        }

    private fun RenderContext.conferenceEventsSideEffect(renderProps: ConferenceProps) {
        val conference = renderProps.conference
        runningSideEffect("conferenceEventsSideEffect($conference)") {
            val events = conference.getConferenceEvents().shareIn(this, SharingStarted.Lazily)
            events
                .mapNotNull(::toConferenceAction)
                .onEach(actionSink::send)
                .launchIn(this)
            events.filterIsInstance<ReferConferenceEvent>()
                .map {
                    val c = conference.referer.refer(it)
                    onReferConferenceEvent(c)
                }
                .onEach(actionSink::send)
                .launchIn(this)
        }
    }

    private fun RenderContext.aspectRatioSideEffect(renderState: ConferenceState) {
        val connection = renderState.connection
        val aspectRatio = renderState.aspectRatio.takeUnless(Float::isNaN) ?: return
        runningSideEffect("aspectRatioSideEffect($connection, $aspectRatio)") {
            connection.setMainRemoteVideoTrackPreferredAspectRatio(aspectRatio)
        }
    }

    private fun toConferenceAction(event: ConferenceEvent) = when (event) {
        is PresentationStartConferenceEvent -> onPresentationStartConferenceEvent(event)
        is PresentationStopConferenceEvent -> onPresentationStopConferenceEvent(event)
        is DisconnectConferenceEvent -> onDisconnectConferenceEvent(event)
        is FailureConferenceEvent -> onFailureConferenceEvent(event)
        else -> null
    }

    private fun onSplashScreen(splashScreen: SplashScreen?) =
        action({ "onSplashScreen($splashScreen)" }) {
            state = state.copy(splashScreen = splashScreen)
        }

    private fun onScreenCapture(data: Intent) = action({ "onScreenCapture($data)" }) {
        state = state.copy(screenCaptureData = data)
    }

    private fun onScreenCaptureVideoTrack(localVideoTrack: LocalVideoTrack) =
        action({ "onScreenCaptureVideoTrack($localVideoTrack)" }) {
            state = state.copy(
                screenCaptureData = null,
                screenCaptureVideoTrack = localVideoTrack,
            )
            state.connection.setPresentationVideoTrack(localVideoTrack)
            localVideoTrack.startCapture(QualityProfile.VeryHigh)
        }

    private fun onStopScreenCapture() = action({ "onStopScreenCapture()" }) {
        state.connection.setPresentationVideoTrack(null)
        state.screenCaptureVideoTrack?.dispose()
        state = state.copy(
            screenCapturing = false,
            screenCaptureVideoTrack = null,
        )
    }

    private fun onAudioDevicesChange(visible: Boolean) =
        action({ "onAudioDevicesChange($visible)" }) {
            state = state.copy(audioDevicesVisible = visible)
        }

    private fun onBandwidthChange(visible: Boolean) = action({ "onBandwidthChange($visible)" }) {
        state = state.copy(bandwidthVisible = visible)
    }

    private fun onBandwidthOutput(output: BandwidthOutput) =
        action({ "onBandwidthOutput($output)" }) {
            if (output is BandwidthOutput.ChangeBandwidth) {
                state.connection.setMaxBitrate(output.bandwidth.bitrate)
            }
            state = state.copy(bandwidthVisible = false)
        }

    private fun onDtmfChange(visible: Boolean) = action({ "onDtmfChange($visible)" }) {
        state = state.copy(dtmfVisible = visible)
    }

    private fun onDtmfOutput(output: DtmfOutput) = action({ "onDtmfOutput($output)" }) {
        when (output) {
            is DtmfOutput.Tone -> state.connection.dtmf(output.tone)
            is DtmfOutput.Back -> state = state.copy(dtmfVisible = false)
        }
    }

    private fun onChatClick() = action({ "onChatClick()" }) {
        state = state.copy(showingChat = true)
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        if (state.showingChat) {
            state = state.copy(showingChat = false)
        } else {
            setOutput(ConferenceOutput.Back)
        }
    }

    private fun onMainRemoteVideoTrack(videoTrack: VideoTrack?) =
        action({ "onMainRemoteVideoTrack($videoTrack)" }) {
            state = state.copy(mainRemoteVideoTrack = videoTrack)
        }

    private fun onScreenCapturing(capturing: Boolean) =
        action({ "onScreenCapturing($capturing)" }) {
            state = state.copy(screenCapturing = capturing)
        }

    private fun onPresentationStartConferenceEvent(event: PresentationStartConferenceEvent) =
        action({ "onPresentationStartConferenceEvent($event)" }) {
            state.connection.setPresentationVideoTrack(null)
            state.screenCaptureVideoTrack?.dispose()
            state.connection.setPresentationRemoteVideoTrackEnabled(true)
            state = state.copy(
                presentation = true,
                screenCapturing = false,
                screenCaptureVideoTrack = null,
            )
        }

    private fun onPresentationStopConferenceEvent(event: PresentationStopConferenceEvent) =
        action({ "onPresentationStopConferenceEvent($event)" }) {
            state.connection.setPresentationRemoteVideoTrackEnabled(false)
            state = state.copy(presentation = false)
        }

    private fun onReferConferenceEvent(conference: Conference) =
        action({ "onReferConferenceEvent($conference)" }) {
            setOutput(ConferenceOutput.Refer(conference))
        }

    private fun onDisconnectConferenceEvent(event: DisconnectConferenceEvent) =
        action({ "onDisconnectConferenceEvent($event)" }) {
            setOutput(ConferenceOutput.Back)
        }

    private fun onFailureConferenceEvent(event: FailureConferenceEvent) =
        action({ "onFailureConferenceEvent($event)" }) {
            setOutput(ConferenceOutput.Back)
        }

    private fun onPresentationRemoteVideoTrack(videoTrack: VideoTrack?) =
        action({ "onPresentationRemoteVideoTrack($videoTrack)" }) {
            state = state.copy(presentationRemoteVideoTrack = videoTrack)
        }

    private fun onAudioDeviceOutput(output: AudioDeviceOutput) =
        action({ "onAudioDeviceOutput($output)" }) {
            when (output) {
                is AudioDeviceOutput.Back -> state = state.copy(audioDevicesVisible = false)
            }
        }

    private fun onChatOutput(output: ChatOutput) = action({ "onChatOutput($output)" }) {
        when (output) {
            is ChatOutput.Back -> state = state.copy(showingChat = false)
        }
    }

    private fun onAspectRatioChange(aspectRatio: Float) =
        action({ "onAspectRatioChange($aspectRatio)" }) {
            state = state.copy(aspectRatio = aspectRatio)
        }
}
