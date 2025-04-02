/*
 * Copyright 2022-2025 Pexip AS
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
package com.pexip.sdk.sample.permissions

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import javax.inject.Inject

class PermissionsWorkflow @Inject constructor() :
    StatefulWorkflow<PermissionsProps, PermissionsState, PermissionsOutput, PermissionsScreen>() {

    override fun initialState(props: PermissionsProps, snapshot: Snapshot?): PermissionsState =
        PermissionsState()

    override fun snapshotState(state: PermissionsState): Snapshot? = null

    override fun render(
        renderProps: PermissionsProps,
        renderState: PermissionsState,
        context: RenderContext,
    ): PermissionsScreen = PermissionsScreen(
        permissions = renderProps.permissions,
        onPermissionsRequestResult = context.send(::onPermissionsRequestResult),
        onBackClick = context.send(::onBackClick),
    )

    private fun onPermissionsRequestResult(result: PermissionsRequestResult) =
        action({ "onPermissionsRequestResult($result)" }) {
            val output = when {
                result.grants.all { it.value } -> PermissionsOutput.Next
                result.rationales.none {
                    it.value
                } &&
                    state.rationales?.none { it.value } == true -> {
                    PermissionsOutput.ApplicationDetailsSettings
                }
                else -> null
            }
            output?.let(::setOutput)
            state = PermissionsState(result.rationales)
        }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(PermissionsOutput.Back)
    }
}
