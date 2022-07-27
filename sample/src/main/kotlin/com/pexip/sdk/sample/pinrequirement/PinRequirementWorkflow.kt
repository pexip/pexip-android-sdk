package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NodeResolver
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequiredPinException
import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinRequirementWorkflow @Inject constructor(
    private val store: SettingsStore,
    private val resolver: NodeResolver,
    private val service: InfinityService,
) : StatefulWorkflow<PinRequirementProps, PinRequirementState, PinRequirementOutput, PinRequirementRendering>() {

    override fun initialState(
        props: PinRequirementProps,
        snapshot: Snapshot?,
    ): PinRequirementState = snapshot?.toParcelable() ?: PinRequirementState.ResolvingNode

    override fun snapshotState(state: PinRequirementState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: PinRequirementProps,
        renderState: PinRequirementState,
        context: RenderContext,
    ): PinRequirementRendering {
        if (renderState is PinRequirementState.ResolvingNode) {
            context.getNodeSideEffect(renderProps)
        }
        if (renderState is PinRequirementState.ResolvingPinRequirement) {
            context.getPinRequirementSideEffect(renderProps, renderState)
        }
        return when (renderState) {
            is PinRequirementState.ResolvingNode -> PinRequirementRendering.ResolvingPinRequirement
            is PinRequirementState.ResolvingPinRequirement -> PinRequirementRendering.ResolvingPinRequirement
            is PinRequirementState.Failure -> PinRequirementRendering.Failure(
                t = renderState.t,
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.getNodeSideEffect(props: PinRequirementProps) =
        runningSideEffect(props.toString()) {
            val action = runCatching { resolver.resolve(props.host).await() }
                .mapCatching { it.first { node -> service.newRequest(node).status().await() } }
                .fold(::OnNode, ::OnError)
            actionSink.send(action)
        }

    private fun RenderContext.getPinRequirementSideEffect(
        props: PinRequirementProps,
        state: PinRequirementState.ResolvingPinRequirement,
    ) = runningSideEffect("$props:${state.node}") {
        val action = runCatching { store.getDisplayName().first() }
            .mapCatching { RequestTokenRequest(displayName = it) }
            .mapCatching {
                service.newRequest(state.node)
                    .conference(props.conferenceAlias)
                    .requestToken(it)
                    .await()
            }
            .fold(
                onSuccess = { OnResponse(state.node, it) },
                onFailure = {
                    when (it) {
                        is RequiredPinException -> OnRequiredPin(state.node, it.guestPin)
                        else -> OnError(it)
                    }
                }
            )
        actionSink.send(action)
    }
}
