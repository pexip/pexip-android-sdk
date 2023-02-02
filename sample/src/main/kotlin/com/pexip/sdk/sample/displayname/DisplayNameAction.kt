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
package com.pexip.sdk.sample.displayname

import com.squareup.workflow1.WorkflowAction

typealias DisplayNameAction = WorkflowAction<Unit, DisplayNameState, DisplayNameOutput>

data class OnDisplayNameChange(val displayName: String) : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayName = displayName)
    }
}

class OnDisplayNameSet : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = null)
        setOutput(DisplayNameOutput.Next)
    }
}

class OnNextClick : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = state.displayName)
    }
}

class OnBackClick : DisplayNameAction() {

    override fun Updater.apply() {
        setOutput(DisplayNameOutput.Back)
    }
}
