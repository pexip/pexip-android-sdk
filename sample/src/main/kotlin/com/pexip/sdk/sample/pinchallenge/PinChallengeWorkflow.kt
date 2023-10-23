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

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinChallengeWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<PinChallengeProps, PinChallengeState, PinChallengeOutput, PinChallengeRendering>() {

    override fun initialState(props: PinChallengeProps, snapshot: Snapshot?): PinChallengeState =
        PinChallengeState()

    override fun snapshotState(state: PinChallengeState): Snapshot? = null

    override fun render(
        renderProps: PinChallengeProps,
        renderState: PinChallengeState,
        context: RenderContext,
    ): PinChallengeRendering {
        context.verifyPinSideEffect(renderProps, renderState)
        return PinChallengeRendering(
            pin = renderState.pin,
            error = renderState.t != null,
            submitEnabled = when {
                renderState.requesting -> false
                renderProps.required -> renderState.pin.isNotBlank()
                else -> true
            },
            onPinChange = context.send(::OnPinChange),
            onSubmitClick = context.send(::OnSubmitClick),
            onBackClick = context.send(::OnBackClick),
        )
    }

    private fun RenderContext.verifyPinSideEffect(
        props: PinChallengeProps,
        state: PinChallengeState,
    ) {
        val pinToSubmit = state.pinToSubmit ?: return
        runningSideEffect("$props:$pinToSubmit") {
            actionSink.send(OnRequestToken())
            val action = try {
                val displayName = store.getDisplayName().first()
                val request = RequestTokenRequest(displayName = displayName, directMedia = true)
                val response = props.step.requestToken(request, pinToSubmit).await()
                val conference = InfinityConference.create(props.step, response)
                OnConference(conference)
            } catch (e: InvalidPinException) {
                OnInvalidPin(e)
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
    }
}
