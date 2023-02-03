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
package com.pexip.sdk.sample.pinchallenge

import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.squareup.workflow1.WorkflowAction

typealias PinChallengeAction = WorkflowAction<PinChallengeProps, PinChallengeState, PinChallengeOutput>

class OnRequestToken : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(requesting = true)
    }
}

data class OnPinChange(val pin: String) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(pin = pin.trim(), t = null)
    }
}

class OnSubmitClick : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(pinToSubmit = state.pin)
    }
}

class OnBackClick : PinChallengeAction() {

    override fun Updater.apply() {
        setOutput(PinChallengeOutput.Back)
    }
}

data class OnResponse(val response: RequestTokenResponse) : PinChallengeAction() {

    override fun Updater.apply() {
        setOutput(PinChallengeOutput.Response(response))
    }
}

data class OnInvalidPin(val e: InvalidPinException) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(
            pin = "",
            t = e,
            requesting = false,
            pinToSubmit = null,
        )
    }
}

data class OnError(val t: Throwable) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(
            t = t,
            requesting = false,
            pinToSubmit = null,
        )
    }
}
