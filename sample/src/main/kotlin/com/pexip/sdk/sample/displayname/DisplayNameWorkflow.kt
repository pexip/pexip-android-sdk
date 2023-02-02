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
package com.pexip.sdk.sample.displayname

import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayNameWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<Unit, DisplayNameState, DisplayNameOutput, DisplayNameRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): DisplayNameState =
        DisplayNameState()

    override fun snapshotState(state: DisplayNameState): Snapshot? = null

    override fun render(
        renderProps: Unit,
        renderState: DisplayNameState,
        context: RenderContext,
    ): DisplayNameRendering {
        context.getDisplayNameSideEffect()
        context.setDisplayNameSideEffect(renderState.displayNameToSet)
        return DisplayNameRendering(
            displayName = renderState.displayName,
            onDisplayNameChange = context.send(::OnDisplayNameChange),
            onNextClick = context.send(::OnNextClick),
            onBackClick = context.send(::OnBackClick),
        )
    }

    private fun RenderContext.getDisplayNameSideEffect() = runningSideEffect("getDisplayName") {
        val displayName = store.getDisplayName().first()
        val action = OnDisplayNameChange(displayName)
        actionSink.send(action)
    }

    private fun RenderContext.setDisplayNameSideEffect(displayNameToSet: String?) {
        val displayName = (displayNameToSet?.takeIf { it.isNotBlank() } ?: return).trim()
        runningSideEffect("setDisplayName($displayName)") {
            store.setDisplayName(displayName)
            val action = OnDisplayNameSet()
            actionSink.send(action)
        }
    }
}
