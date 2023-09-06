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
package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NodeResolver
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequiredPinException
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinRequirementWorkflow @Inject constructor(
    private val store: SettingsStore,
    private val resolver: NodeResolver,
    private val service: InfinityService,
) : StatefulWorkflow<PinRequirementProps, PinRequirementState, PinRequirementOutput, Unit>() {

    override fun initialState(
        props: PinRequirementProps,
        snapshot: Snapshot?,
    ): PinRequirementState = PinRequirementState.ResolvingNode

    override fun snapshotState(state: PinRequirementState): Snapshot? = null

    override fun render(
        renderProps: PinRequirementProps,
        renderState: PinRequirementState,
        context: RenderContext,
    ) {
        if (renderState is PinRequirementState.ResolvingNode) {
            context.getNodeSideEffect(renderProps)
        } else if (renderState is PinRequirementState.ResolvingPinRequirement) {
            context.getPinRequirementSideEffect(renderProps, renderState)
        }
    }

    private fun RenderContext.getNodeSideEffect(props: PinRequirementProps) =
        runningSideEffect(props.toString()) {
            val action = runCatching { resolver.resolve(props.host).await() }
                .map { it.asSequence().map(service::newRequest) }
                .mapCatching { it.first { builder -> builder.status().await() } }
                .fold(::OnNode, ::OnError)
            actionSink.send(action)
        }

    private fun RenderContext.getPinRequirementSideEffect(
        props: PinRequirementProps,
        state: PinRequirementState.ResolvingPinRequirement,
    ) = runningSideEffect("${props.conferenceAlias}:${state.builder}") {
        val step = state.builder.conference(props.conferenceAlias)
        val action = runCatching { store.getDisplayName().first() }
            .mapCatching { RequestTokenRequest(displayName = it) }
            .mapCatching { step.requestToken(it).await() }
            .map { InfinityConference.create(step, it) }
            .fold(
                onSuccess = ::OnConference,
                onFailure = {
                    when (it) {
                        is RequiredPinException -> OnRequiredPin(
                            step = step,
                            required = it.guestPin,
                        )
                        else -> OnError(it)
                    }
                },
            )
        actionSink.send(action)
    }
}
