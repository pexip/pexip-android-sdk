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
import com.pexip.sdk.sample.pinrequirement.PinRequirementWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AliasWorkflow @Inject constructor(private val pinRequirementWorkflow: PinRequirementWorkflow) :
    StatefulWorkflow<Unit, AliasState, AliasOutput, AliasRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): AliasState = AliasState()

    override fun snapshotState(state: AliasState): Snapshot? = null

    override fun render(
        renderProps: Unit,
        renderState: AliasState,
        context: RenderContext,
    ): AliasRendering {
        context.runningWorker(renderState.blankAliasWorker, handler = ::onBlankAliasWorkerOutput)
        context.renderPinRequirementWorkflow(renderState.pinRequirementProps)
        return AliasRendering(
            alias = renderState.alias,
            host = renderState.host,
            presentationInMain = renderState.presentationInMain,
            resolveEnabled = !renderState.blankAlias && renderState.pinRequirementProps == null,
            onPresentationInMainChange = context.send(::onPresentationInMainChange),
            onResolveClick = context.send(::onResolveClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun RenderContext.renderPinRequirementWorkflow(props: PinRequirementProps?) {
        props ?: return
        renderChild(pinRequirementWorkflow, props, handler = ::onPinRequirementOutput)
    }

    private fun onPresentationInMainChange(presentationInMain: Boolean) =
        action({ "onPresentationInMainChange($presentationInMain)" }) {
            state = state.copy(presentationInMain = presentationInMain)
        }

    private fun onBlankAliasWorkerOutput(blankAlias: Boolean) =
        action({ "onBlankAliasWorkerOutput($blankAlias)" }) {
            state = state.copy(blankAlias = blankAlias)
        }

    private fun onPinRequirementOutput(output: PinRequirementOutput) =
        action({ "onPinRequirementOutput($output)" }) {
            state = state.copy(pinRequirementProps = null)
            val o = when (output) {
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
            setOutput(o)
        }

    private fun onResolveClick() = action({ "onResolveClick()" }) {
        val alias = state.alias.textValue
        val props = PinRequirementProps(
            alias = alias,
            host = state.host.textValue.trim().ifBlank { alias.split("@").last() },
        )
        state = state.copy(pinRequirementProps = props)
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(AliasOutput.Back)
    }
}
