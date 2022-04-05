package com.pexip.sdk.video.sample.alias

import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

class AliasWorkflow : StatefulWorkflow<Unit, AliasState, AliasOutput, AliasRendering>() {

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
        onAliasChange = context.send(::OnAliasChange),
        onHostChange = context.send(::OnHostChange),
        onResolveClick = context.send(::OnResolveClick),
        onBackClick = context.send(::OnBackClick)
    )
}
