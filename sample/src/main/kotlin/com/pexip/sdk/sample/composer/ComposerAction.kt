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
package com.pexip.sdk.sample.composer

import com.squareup.workflow1.WorkflowAction

typealias ComposerAction = WorkflowAction<Unit, ComposerState, ComposerOutput>

class OnMessageChange(private val message: String) : ComposerAction() {

    override fun Updater.apply() {
        state = ComposerState(message)
    }
}

class OnSubmitClick : ComposerAction() {

    override fun Updater.apply() {
        val message = requireNotNull(state.message.takeIf { it.isNotBlank() })
        state = ComposerState()
        setOutput(ComposerOutput.Submit(message.trim()))
    }
}
