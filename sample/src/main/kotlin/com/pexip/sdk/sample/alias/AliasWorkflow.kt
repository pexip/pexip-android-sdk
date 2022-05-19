package com.pexip.sdk.sample.alias

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AliasWorkflow @Inject constructor() :
    StatefulWorkflow<Unit, AliasState, AliasOutput, AliasRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): AliasState =
        snapshot?.toParcelable() ?: AliasState()

    override fun snapshotState(state: AliasState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: AliasState,
        context: RenderContext,
    ): AliasRendering = AliasRendering(
        alias = renderState.alias,
        host = renderState.host,
        presentationInMain = renderState.presentationInMain,
        onAliasChange = context.send(::OnAliasChange),
        onHostChange = context.send(::OnHostChange),
        onPresentationInMainChange = context.send(::OnPresentationInMainChange),
        onResolveClick = context.send(::OnResolveClick),
        onBackClick = context.send(::OnBackClick)
    )
}
