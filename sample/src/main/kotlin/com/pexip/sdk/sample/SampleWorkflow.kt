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
package com.pexip.sdk.sample

import android.Manifest
import android.content.Context
import android.os.Build
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.CameraVideoTrackFactory
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalAudioTrackFactory
import com.pexip.sdk.sample.conference.ConferenceProps
import com.pexip.sdk.sample.conference.ConferenceWorkflow
import com.pexip.sdk.sample.permissions.PermissionsProps
import com.pexip.sdk.sample.permissions.PermissionsWorkflow
import com.pexip.sdk.sample.preflight.PreflightProps
import com.pexip.sdk.sample.preflight.PreflightWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
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

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState {
        val allGranted = Permissions.all(context::isPermissionGranted)
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
                props = PermissionsProps(Permissions),
                handler = ::OnPermissionsOutput,
            )
            is SampleDestination.Preflight -> context.renderChild(
                child = preflightWorkflow,
                props = PreflightProps(
                    cameraVideoTrack = renderState.cameraVideoTrack,
                    microphoneAudioTrack = renderState.microphoneAudioTrack,
                ),
                handler = ::OnPreflightOutput,
            )
            is SampleDestination.Conference -> context.renderChild(
                child = conferenceWorkflow,
                props = ConferenceProps(
                    node = destination.node,
                    conferenceAlias = destination.conferenceAlias,
                    presentationInMain = destination.presentationInMain,
                    response = destination.response,
                    cameraVideoTrack = renderState.cameraVideoTrack,
                    microphoneAudioTrack = renderState.microphoneAudioTrack,
                ),
                handler = ::OnConferenceOutput,
            )
        }
    }

    private fun RenderContext.createCameraVideoTrackSideEffect(count: UInt) {
        if (count > 0u) {
            runningSideEffect("createCameraVideoTrackSideEffect($count)") {
                val callback = object : CameraVideoTrack.Callback {

                    override fun onCameraDisconnected() {
                        val action = OnCameraVideoTrackChange(null)
                        actionSink.send(action)
                    }
                }
                val track = cameraVideoTrackFactory.createCameraVideoTrack(callback)
                val action = OnCameraVideoTrackChange(track)
                actionSink.send(action)
            }
        }
    }

    private fun RenderContext.createMicrophoneAudioTrackSideEffect(count: UInt) {
        if (count > 0u) {
            runningSideEffect("createMicrophoneAudioTrackSideEffect($count)") {
                val track = localAudioTrackFactory.createLocalAudioTrack()
                val action = OnMicrophoneAudioTrackChange(track)
                actionSink.send(action)
            }
        }
    }

    private fun RenderContext.cameraVideoTrackSideEffect(track: CameraVideoTrack?) {
        if (track != null) {
            runningSideEffect("cameraVideoTrackSideEffect($track)") {
                try {
                    track.startCapture()
                    awaitCancellation()
                } finally {
                    actionSink.send(OnCameraVideoTrackChange(null))
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
                    actionSink.send(OnMicrophoneAudioTrackChange(null))
                    track.stopCapture()
                    track.dispose()
                }
            }
        }
    }

    companion object {

        private val Permissions = buildSet {
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}
