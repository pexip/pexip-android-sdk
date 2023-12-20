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
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayNameWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<Unit, DisplayNameState, DisplayNameOutput, DisplayNameRendering>() {

    private val initialDisplayNameWorker = Worker.from(store.getDisplayName()::first)

    override fun initialState(props: Unit, snapshot: Snapshot?): DisplayNameState =
        DisplayNameState()

    override fun snapshotState(state: DisplayNameState): Snapshot? = null

    override fun render(
        renderProps: Unit,
        renderState: DisplayNameState,
        context: RenderContext,
    ): DisplayNameRendering {
        context.runningWorker(
            worker = initialDisplayNameWorker,
            handler = ::onInitialDisplayNameWorkerOutput,
        )
        if (renderState.displayNameWorker != null) {
            context.runningWorker(
                worker = renderState.displayNameWorker,
                handler = ::onSetDisplayNameWorkerOutput,
            )
        }
        return DisplayNameRendering(
            displayName = renderState.displayName,
            onNextClick = context.send(::onNextClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun onInitialDisplayNameWorkerOutput(displayName: String) =
        action({ "onInitialDisplayNameWorkerOutput($displayName" }) {
            state.displayName.textValue = displayName
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onSetDisplayNameWorkerOutput(unit: Unit) =
        action({ "onSetDisplayNameWorkerOutput()" }) {
            setOutput(DisplayNameOutput.Next)
        }

    private fun onNextClick() = action({ "onNextClick()" }) {
        val displayName = state.displayName.textValue
            .trim()
            .takeIf(String::isNotBlank)
            ?: return@action
        state = state.copy(displayNameWorker = Worker.from { store.setDisplayName(displayName) })
    }

    private fun onBackClick() = action({ "OnBackClick()" }) {
        setOutput(DisplayNameOutput.Back)
    }
}
