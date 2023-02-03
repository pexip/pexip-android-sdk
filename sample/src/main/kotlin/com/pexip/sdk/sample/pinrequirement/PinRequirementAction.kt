/*
 * Copyright 2022 Pexip AS
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
package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.squareup.workflow1.WorkflowAction
import java.net.URL

typealias PinRequirementAction = WorkflowAction<PinRequirementProps, PinRequirementState, PinRequirementOutput>

data class OnNode(val node: URL) : PinRequirementAction() {

    override fun Updater.apply() {
        state = PinRequirementState.ResolvingPinRequirement(node)
    }
}

data class OnResponse(
    val node: URL,
    val conferenceAlias: String,
    val response: RequestTokenResponse,
) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.None(node, conferenceAlias, response))
    }
}

data class OnRequiredPin(
    val node: URL,
    val conferenceAlias: String,
    val required: Boolean,
) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Some(node, conferenceAlias, required))
    }
}

data class OnError(val t: Throwable) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Error(t))
    }
}
