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
package com.pexip.sdk.sample.preflight

import com.pexip.sdk.sample.alias.AliasOutput
import com.pexip.sdk.sample.alias.AliasWorkflow
import com.pexip.sdk.sample.displayname.DisplayNameOutput
import com.pexip.sdk.sample.displayname.DisplayNameWorkflow
import com.pexip.sdk.sample.media.LocalMediaTrackProps
import com.pexip.sdk.sample.media.LocalMediaTrackWorkflow
import com.pexip.sdk.sample.pinchallenge.PinChallengeOutput
import com.pexip.sdk.sample.pinchallenge.PinChallengeProps
import com.pexip.sdk.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import javax.inject.Inject

class PreflightWorkflow @Inject constructor(
    private val displayNameWorkflow: DisplayNameWorkflow,
    private val aliasWorkflow: AliasWorkflow,
    private val pinChallengeWorkflow: PinChallengeWorkflow,
    private val localMediaTrackWorkflow: LocalMediaTrackWorkflow,
) : StatefulWorkflow<PreflightProps, PreflightState, PreflightOutput, PreflightRendering>() {

    override fun initialState(props: PreflightProps, snapshot: Snapshot?): PreflightState =
        PreflightState()

    override fun snapshotState(state: PreflightState): Snapshot? = null

    override fun render(
        renderProps: PreflightProps,
        renderState: PreflightState,
        context: RenderContext,
    ): PreflightRendering = PreflightRendering(
        childRendering = when (val destination = renderState.destination) {
            is PreflightDestination.DisplayName -> context.renderChild(
                child = displayNameWorkflow,
                handler = ::onDisplayNameOutput,
            )
            is PreflightDestination.Alias -> context.renderChild(
                child = aliasWorkflow,
                handler = ::onAliasOutput,
            )
            is PreflightDestination.PinChallenge -> context.renderChild(
                child = pinChallengeWorkflow,
                props = PinChallengeProps(
                    step = destination.step,
                    required = destination.required,
                ),
                handler = ::onPinChallengeOutput,
            )
            null -> null
        },
        cameraVideoTrack = renderProps.cameraVideoTrack,
        callEnabled = with(renderProps) { cameraVideoTrack != null && microphoneAudioTrack != null },
        onCallClick = context.send(::onCallClick),
        onCreateCameraVideoTrackClick = context.send(::onCreateCameraVideoTrackClick),
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
        onBackClick = context.send(::onBackClick),
    )

    private fun onCallClick() = action({ "onCallClick()" }) {
        state = PreflightState(PreflightDestination.DisplayName)
    }

    private fun onCreateCameraVideoTrackClick() = action({ "onCreateCameraVideoTrackClick()" }) {
        setOutput(PreflightOutput.CreateCameraVideoTrack)
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(PreflightOutput.Back)
    }

    private fun onDisplayNameOutput(output: DisplayNameOutput) =
        action({ "onDisplayNameOutput($output)" }) {
            val destination = when (output) {
                is DisplayNameOutput.Next -> PreflightDestination.Alias
                is DisplayNameOutput.Back -> null
            }
            state = PreflightState(destination)
        }

    private fun onAliasOutput(output: AliasOutput) = action({ "onAliasOutput($output)" }) {
        when (output) {
            is AliasOutput.Conference -> {
                val o = PreflightOutput.Conference(
                    conference = output.conference,
                    presentationInMain = output.presentationInMain,
                )
                setOutput(o)
            }
            is AliasOutput.PinChallenge -> state = PreflightState(
                destination = PreflightDestination.PinChallenge(
                    step = output.step,
                    presentationInMain = output.presentationInMain,
                    required = output.required,
                ),
            )
            is AliasOutput.Toast -> setOutput(PreflightOutput.Toast(output.message))
            is AliasOutput.Back -> state = PreflightState(null)
        }
    }

    private fun onPinChallengeOutput(output: PinChallengeOutput) =
        action({ "onPinChallengeOutput($output)" }) {
            val s = checkNotNull(state.destination as? PreflightDestination.PinChallenge)
            when (output) {
                is PinChallengeOutput.Conference -> {
                    val o = PreflightOutput.Conference(
                        conference = output.conference,
                        presentationInMain = s.presentationInMain,
                    )
                    setOutput(o)
                }
                is PinChallengeOutput.Back -> state = PreflightState(null)
            }
        }
}
