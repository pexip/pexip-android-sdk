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
import com.pexip.sdk.sample.displayname.DisplayNameOutput
import com.pexip.sdk.sample.pinchallenge.PinChallengeOutput
import com.squareup.workflow1.WorkflowAction

typealias PreflightAction = WorkflowAction<PreflightProps, PreflightState, PreflightOutput>

class OnCallClick : PreflightAction() {

    override fun Updater.apply() {
        state = PreflightState(PreflightDestination.DisplayName)
    }
}

class OnCreateCameraVideoTrackClick : PreflightAction() {

    override fun Updater.apply() {
        setOutput(PreflightOutput.CreateCameraVideoTrack)
    }
}

class OnBackClick : PreflightAction() {

    override fun Updater.apply() {
        setOutput(PreflightOutput.Back)
    }
}

class OnDisplayNameOutput(private val output: DisplayNameOutput) : PreflightAction() {

    override fun Updater.apply() {
        val destination = when (output) {
            is DisplayNameOutput.Next -> PreflightDestination.Alias
            is DisplayNameOutput.Back -> null
        }
        state = PreflightState(destination)
    }
}

class OnAliasOutput(private val output: AliasOutput) : PreflightAction() {

    override fun Updater.apply() {
        when (output) {
            is AliasOutput.Conference -> {
                val output = PreflightOutput.Conference(
                    builder = output.builder,
                    conferenceAlias = output.conferenceAlias,
                    presentationInMain = output.presentationInMain,
                    response = output.response,
                )
                setOutput(output)
            }
            is AliasOutput.PinChallenge -> state = PreflightState(
                destination = PreflightDestination.PinChallenge(
                    builder = output.builder,
                    conferenceAlias = output.conferenceAlias,
                    presentationInMain = output.presentationInMain,
                    required = output.required,
                ),
            )
            is AliasOutput.Toast -> setOutput(PreflightOutput.Toast(output.message))
            is AliasOutput.Back -> state = PreflightState(null)
        }
    }
}

class OnPinChallengeOutput(private val output: PinChallengeOutput) : PreflightAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state.destination as? PreflightDestination.PinChallenge)
        when (output) {
            is PinChallengeOutput.Response -> {
                val output = PreflightOutput.Conference(
                    builder = s.builder,
                    conferenceAlias = s.conferenceAlias,
                    presentationInMain = s.presentationInMain,
                    response = output.response,
                )
                setOutput(output)
            }
            is PinChallengeOutput.Back -> state = PreflightState(null)
        }
    }
}
