package com.pexip.sdk.sample.alias

import com.pexip.sdk.sample.pinrequirement.PinRequirementProps
import com.pexip.sdk.sample.pinrequirement.PinRequirementWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
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
        context.renderPinRequirementWorkflow(renderState.pinRequirementProps)
        return AliasRendering(
            alias = renderState.conferenceAlias,
            host = renderState.host,
            presentationInMain = renderState.presentationInMain,
            resolveEnabled = renderState.conferenceAlias.isNotBlank() && renderState.pinRequirementProps == null,
            onAliasChange = context.send(::OnAliasChange),
            onHostChange = context.send(::OnHostChange),
            onPresentationInMainChange = context.send(::OnPresentationInMainChange),
            onResolveClick = context.send(::OnResolveClick),
            onBackClick = context.send(::OnBackClick),
        )
    }

    private fun RenderContext.renderPinRequirementWorkflow(props: PinRequirementProps?) {
        props ?: return
        renderChild(pinRequirementWorkflow, props, handler = ::OnPinRequirementOutput)
    }
}
