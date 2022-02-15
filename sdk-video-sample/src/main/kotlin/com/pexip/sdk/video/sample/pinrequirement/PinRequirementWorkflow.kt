package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.video.TokenRequester
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

class PinRequirementWorkflow(private val requester: TokenRequester) :
    StatefulWorkflow<PinRequirementProps, PinRequirementState, PinRequirementOutput, PinRequirementRendering>() {

    override fun initialState(
        props: PinRequirementProps,
        snapshot: Snapshot?,
    ): PinRequirementState = snapshot?.toParcelable() ?: PinRequirementState.ResolvingPinRequirement

    override fun snapshotState(state: PinRequirementState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: PinRequirementProps,
        renderState: PinRequirementState,
        context: RenderContext,
    ): PinRequirementRendering {
        context.getPinRequirementSideEffect(renderProps)
        return when (renderState) {
            is PinRequirementState.ResolvingPinRequirement -> PinRequirementRendering.ResolvingPinRequirement
            is PinRequirementState.Failure -> PinRequirementRendering.Failure(
                t = renderState.t,
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.getPinRequirementSideEffect(props: PinRequirementProps) =
        runningSideEffect(props.toString()) {
            val action = try {
                val token = requester.requestToken(
                    nodeAddress = props.nodeAddress,
                    alias = props.alias,
                    displayName = props.displayName,
                    pin = null
                )
                OnToken(token)
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
}
