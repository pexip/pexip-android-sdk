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
package com.pexip.sdk.sample.composer

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComposerWorkflow @Inject constructor() :
    StatefulWorkflow<Unit, ComposerState, ComposerOutput, ComposerRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): ComposerState = ComposerState()

    override fun snapshotState(state: ComposerState): Snapshot? = null

    override fun render(
        renderProps: Unit,
        renderState: ComposerState,
        context: RenderContext,
    ): ComposerRendering = ComposerRendering(
        message = renderState.message,
        submitEnabled = renderState.submitEnabled,
        onMessageChange = context.send(::OnMessageChange),
        onSubmitClick = context.send(::OnSubmitClick),
    )
}
