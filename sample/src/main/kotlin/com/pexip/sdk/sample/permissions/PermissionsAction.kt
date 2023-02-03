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
package com.pexip.sdk.sample.permissions

import com.squareup.workflow1.WorkflowAction

typealias PermissionsAction = WorkflowAction<PermissionsProps, PermissionsState, PermissionsOutput>

class OnPermissionsRequestResult(private val result: PermissionsRequestResult) :
    PermissionsAction() {

    override fun Updater.apply() {
        val output = when {
            result.grants.all() -> PermissionsOutput.Next
            result.rationales.none() && state.rationales?.none() == true -> {
                PermissionsOutput.ApplicationDetailsSettings
            }
            else -> null
        }
        output?.let(::setOutput)
        state = PermissionsState(result.rationales)
    }

    private fun <T> Map<T, Boolean>.all() = all { it.value }
    private fun <T> Map<T, Boolean>.none() = none { it.value }
}

class OnBackClick : PermissionsAction() {

    override fun Updater.apply() {
        setOutput(PermissionsOutput.Back)
    }
}
