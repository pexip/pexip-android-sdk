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

import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.sample.conference.ConferenceOutput
import com.pexip.sdk.sample.permissions.PermissionsOutput
import com.pexip.sdk.sample.preflight.PreflightOutput
import com.squareup.workflow1.WorkflowAction

typealias SampleAction = WorkflowAction<Unit, SampleState, SampleOutput>

class OnCameraVideoTrackChange(private val track: CameraVideoTrack?) : SampleAction() {

    override fun Updater.apply() {
        state = state.copy(cameraVideoTrack = track)
    }
}

class OnMicrophoneAudioTrackChange(private val track: LocalAudioTrack?) : SampleAction() {

    override fun Updater.apply() {
        state = state.copy(microphoneAudioTrack = track)
    }
}

class OnPermissionsOutput(private val output: PermissionsOutput) : SampleAction() {

    override fun Updater.apply() {
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
}

class OnPreflightOutput(private val output: PreflightOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is PreflightOutput.Conference -> state = state.copy(
                destination = SampleDestination.Conference(
                    builder = output.builder,
                    conferenceAlias = output.conferenceAlias,
                    presentationInMain = output.presentationInMain,
                    response = output.response,
                ),
            )
            is PreflightOutput.Toast -> setOutput(SampleOutput.Toast(output.message))
            is PreflightOutput.CreateCameraVideoTrack -> state = state.copy(
                createCameraVideoTrackCount = state.createCameraVideoTrackCount + 1u,
            )
            is PreflightOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }
}

class OnConferenceOutput(private val output: ConferenceOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is ConferenceOutput.Back -> {
                state = state.copy(destination = SampleDestination.Preflight)
            }
        }
    }
}
