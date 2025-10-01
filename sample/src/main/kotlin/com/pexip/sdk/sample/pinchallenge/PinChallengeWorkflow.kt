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
package com.pexip.sdk.sample.pinchallenge

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private typealias PinChallengeRenderContext =
    StatefulWorkflow.RenderContext<PinChallengeProps, PinChallengeState, PinChallengeOutput>

@Suppress("ktlint:standard:max-line-length")
@Singleton
class PinChallengeWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<PinChallengeProps, PinChallengeState, PinChallengeOutput, PinChallengeScreen>() {

    override fun initialState(props: PinChallengeProps, snapshot: Snapshot?): PinChallengeState =
        PinChallengeState()

    override fun snapshotState(state: PinChallengeState): Snapshot? = null

    override fun render(
        renderProps: PinChallengeProps,
        renderState: PinChallengeState,
        context: PinChallengeRenderContext,
    ): PinChallengeScreen {
        context.runningWorker(renderState.blankPinWorker, handler = ::onBlankPinWorkerOutput)
        if (renderState.pinChallengeWorker != null) {
            context.runningWorker(
                worker = renderState.pinChallengeWorker,
                handler = ::onPinChallengeWorkerOutput,
            )
        }
        return PinChallengeScreen(
            pin = renderState.pin,
            error = renderState.t != null,
            submitEnabled = when {
                renderState.pinChallengeWorker != null -> false
                renderProps.required -> !renderState.blankPin
                else -> true
            },
            onSubmitClick = context.send(::onSubmitClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun onBlankPinWorkerOutput(value: Boolean) =
        action({ "onBlankPinWorkerOutput($value)" }) {
            state = state.copy(blankPin = value)
        }

    private fun onPinChallengeWorkerOutput(result: Result<Conference>) =
        action({ "onPinChallengeWorkerOutput($result)" }) {
            result.onSuccess {
                setOutput(PinChallengeOutput.Conference(it))
            }
            result.onFailure {
                if (it is InvalidPinException) {
                    state.pin.textValue = ""
                }
            }
            state = state.copy(
                t = result.fold(onSuccess = { null }, onFailure = { it }),
                pinChallengeWorker = null,
            )
        }

    private fun onSubmitClick() = action({ "onSubmitClick()" }) {
        val worker = PinChallengeWorker(
            step = props.step,
            pin = state.pin.textValue.trim(),
        )
        state = state.copy(pinChallengeWorker = worker)
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(PinChallengeOutput.Back)
    }

    private inner class PinChallengeWorker(
        private val step: InfinityService.ConferenceStep,
        private val pin: String,
    ) : Worker<Result<Conference>> {

        override fun run(): Flow<Result<Conference>> = flow {
            val result = try {
                val displayName = store.getDisplayName().first()
                val request = RequestTokenRequest(displayName = displayName, directMedia = true)
                val response = step.requestToken(request, pin).await()
                val conference = InfinityConference.create(step, response)
                Result.success(conference)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Result.failure(t)
            }
            emit(result)
        }

        override fun doesSameWorkAs(otherWorker: Worker<*>): Boolean =
            otherWorker is PinChallengeWorker &&
                step == otherWorker.step &&
                pin == otherWorker.pin
    }
}
