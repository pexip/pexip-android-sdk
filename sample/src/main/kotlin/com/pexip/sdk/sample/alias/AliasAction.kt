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
package com.pexip.sdk.sample.alias

import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.sample.pinrequirement.PinRequirementOutput
import com.pexip.sdk.sample.pinrequirement.PinRequirementProps
import com.squareup.workflow1.WorkflowAction

typealias AliasAction = WorkflowAction<Unit, AliasState, AliasOutput>

data class OnAliasChange(val alias: String) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(conferenceAlias = alias.trim())
    }
}

data class OnHostChange(val host: String) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(host = host.trim())
    }
}

data class OnPresentationInMainChange(val presentationInMain: Boolean) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(presentationInMain = presentationInMain)
    }
}

class OnResolveClick : AliasAction() {

    override fun Updater.apply() {
        val props = PinRequirementProps(
            conferenceAlias = state.conferenceAlias,
            host = state.host.ifBlank { state.conferenceAlias.split("@").last() },
        )
        state = state.copy(pinRequirementProps = props)
    }
}

class OnBackClick : AliasAction() {

    override fun Updater.apply() {
        setOutput(AliasOutput.Back)
    }
}

data class OnPinRequirementOutput(val output: PinRequirementOutput) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(pinRequirementProps = null)
        val output = when (output) {
            is PinRequirementOutput.None -> AliasOutput.Conference(
                conference = output.conference,
                presentationInMain = state.presentationInMain,
            )
            is PinRequirementOutput.Some -> AliasOutput.PinChallenge(
                step = output.step,
                presentationInMain = state.presentationInMain,
                required = output.required,
            )
            is PinRequirementOutput.Error -> {
                val message = when (val t = output.t) {
                    is NoSuchConferenceException -> "Conference doesn't exist."
                    is NoSuchNodeException -> "The host doesn't have an Infinity deployment."
                    else -> t.toString()
                }
                AliasOutput.Toast(message)
            }
        }
        setOutput(output)
    }
}
