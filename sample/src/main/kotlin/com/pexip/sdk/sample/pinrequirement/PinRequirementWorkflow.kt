package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenRequest
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
    private val service: InfinityService,
) : StatefulWorkflow<PinRequirementProps, PinRequirementState, PinRequirementOutput, PinRequirementRendering>() {

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
                val displayName = store.getDisplayName().first()
                val request = RequestTokenRequest(displayName = displayName)
                val response = service.newRequest(props.node)
                    .conference(props.conferenceAlias)
                    .requestToken(request)
                    .await()
                OnResponse(response)
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
}
