/*
 * Copyright 2022-2024 Pexip AS
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
package com.pexip.sdk.sample

import android.Manifest
import android.content.Context
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.CameraVideoTrackFactory
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalAudioTrackFactory
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.sample.conference.ConferenceOutput
import com.pexip.sdk.sample.conference.ConferenceProps
import com.pexip.sdk.sample.conference.ConferenceWorkflow
import com.pexip.sdk.sample.permissions.PermissionsOutput
import com.pexip.sdk.sample.permissions.PermissionsProps
import com.pexip.sdk.sample.permissions.PermissionsWorkflow
import com.pexip.sdk.sample.preflight.PreflightOutput
import com.pexip.sdk.sample.preflight.PreflightProps
import com.pexip.sdk.sample.preflight.PreflightWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitCancellation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleWorkflow @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionsWorkflow: PermissionsWorkflow,
    private val preflightWorkflow: PreflightWorkflow,
    private val conferenceWorkflow: ConferenceWorkflow,
    private val localAudioTrackFactory: LocalAudioTrackFactory,
    private val cameraVideoTrackFactory: CameraVideoTrackFactory,
) : StatefulWorkflow<Unit, SampleState, SampleOutput, Any>() {

    private val permissions = buildSet {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState {
        val allGranted = permissions.all(context::isPermissionGranted)
        return SampleState(
            destination = when (allGranted) {
                true -> snapshot?.toParcelable() ?: SampleDestination.Preflight
                else -> SampleDestination.Permissions
            },
            createCameraVideoTrackCount = if (allGranted) 1u else 0u,
            createMicrophoneAudioTrackCount = if (allGranted) 1u else 0u,
        )
    }

    override fun snapshotState(state: SampleState): Snapshot? = null

    override fun render(
        renderProps: Unit,
        renderState: SampleState,
        context: RenderContext,
    ): Any {
        context.createCameraVideoTrackSideEffect(renderState.createCameraVideoTrackCount)
        context.createMicrophoneAudioTrackSideEffect(renderState.createMicrophoneAudioTrackCount)
        context.cameraVideoTrackSideEffect(renderState.cameraVideoTrack)
        context.microphoneAudioTrackSideEffect(renderState.microphoneAudioTrack)
        return when (val destination = renderState.destination) {
            is SampleDestination.Permissions -> context.renderChild(
                child = permissionsWorkflow,
                props = PermissionsProps(permissions),
                handler = ::onPermissionsOutput,
            )
            is SampleDestination.Preflight -> context.renderChild(
                child = preflightWorkflow,
                props = PreflightProps(
                    cameraVideoTrack = renderState.cameraVideoTrack,
                    microphoneAudioTrack = renderState.microphoneAudioTrack,
                ),
                handler = ::onPreflightOutput,
            )
            is SampleDestination.Conference -> context.renderChild(
                child = conferenceWorkflow,
                props = ConferenceProps(
                    conference = destination.conference,
                    presentationInMain = destination.presentationInMain,
                    cameraVideoTrack = renderState.cameraVideoTrack,
                    microphoneAudioTrack = renderState.microphoneAudioTrack,
                ),
                handler = ::onConferenceOutput,
            )
        }
    }

    private fun RenderContext.createCameraVideoTrackSideEffect(count: UInt) {
        if (count > 0u) {
            runningSideEffect("createCameraVideoTrackSideEffect($count)") {
                val callback = object : CameraVideoTrack.Callback {

                    override fun onCameraDisconnected() {
                        val action = onCameraVideoTrackChange(null)
                        actionSink.send(action)
                    }
                }
                val track = cameraVideoTrackFactory.createCameraVideoTrack(callback)
                val action = onCameraVideoTrackChange(track)
                actionSink.send(action)
            }
        }
    }

    private fun RenderContext.createMicrophoneAudioTrackSideEffect(count: UInt) {
        if (count > 0u) {
            runningSideEffect("createMicrophoneAudioTrackSideEffect($count)") {
                val track = localAudioTrackFactory.createLocalAudioTrack()
                val action = onMicrophoneAudioTrackChange(track)
                actionSink.send(action)
            }
        }
    }

    private fun RenderContext.cameraVideoTrackSideEffect(track: CameraVideoTrack?) {
        if (track != null) {
            runningSideEffect("cameraVideoTrackSideEffect($track)") {
                try {
                    track.startCapture(QualityProfile.VeryHigh)
                    awaitCancellation()
                } finally {
                    actionSink.send(onCameraVideoTrackChange(null))
                    track.stopCapture()
                    track.dispose()
                }
            }
        }
    }

    private fun RenderContext.microphoneAudioTrackSideEffect(track: LocalAudioTrack?) {
        if (track != null) {
            runningSideEffect("microphoneAudioTrackSideEffect($track)") {
                try {
                    track.startCapture()
                    awaitCancellation()
                } finally {
                    actionSink.send(onMicrophoneAudioTrackChange(null))
                    track.stopCapture()
                    track.dispose()
                }
            }
        }
    }

    private fun onCameraVideoTrackChange(track: CameraVideoTrack?) =
        action({ "onCameraVideoTrackChange($track" }) {
            state = state.copy(cameraVideoTrack = track)
        }

    private fun onMicrophoneAudioTrackChange(track: LocalAudioTrack?) =
        action({ "onMicrophoneAudioTrackChange($track" }) {
            state = state.copy(microphoneAudioTrack = track)
        }

    private fun onPermissionsOutput(output: PermissionsOutput) =
        action({ "onPermissionsOutput($output)" }) {
            when (output) {
                is PermissionsOutput.ApplicationDetailsSettings -> setOutput(SampleOutput.ApplicationDetailsSettings)
                is PermissionsOutput.Next -> {
                    state = state.copy(
                        destination = SampleDestination.Preflight,
                        createCameraVideoTrackCount = 1u,
                        createMicrophoneAudioTrackCount = 1u,
                    )
                }
                is PermissionsOutput.Back -> setOutput(SampleOutput.Finish)
            }
        }

    private fun onPreflightOutput(output: PreflightOutput) =
        action({ "onPreflightOutput($output)" }) {
            when (output) {
                is PreflightOutput.Conference -> state = state.copy(
                    destination = SampleDestination.Conference(
                        conference = output.conference,
                        presentationInMain = output.presentationInMain,
                    ),
                )
                is PreflightOutput.Toast -> setOutput(SampleOutput.Toast(output.message))
                is PreflightOutput.CreateCameraVideoTrack -> state = state.copy(
                    createCameraVideoTrackCount = state.createCameraVideoTrackCount + 1u,
                )
                is PreflightOutput.Back -> setOutput(SampleOutput.Finish)
            }
        }

    private fun onConferenceOutput(output: ConferenceOutput) =
        action({ "onConferenceOutput($output)" }) {
            val destination = checkNotNull(state.destination as? SampleDestination.Conference)
            val newDestination = when (output) {
                is ConferenceOutput.Refer -> destination.copy(conference = output.conference)
                is ConferenceOutput.Back -> SampleDestination.Preflight
            }
            state = state.copy(destination = newDestination)
        }
}
