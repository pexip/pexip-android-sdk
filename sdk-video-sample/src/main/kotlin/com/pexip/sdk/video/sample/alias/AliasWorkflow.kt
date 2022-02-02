package com.pexip.sdk.video.sample.alias

import android.util.Patterns
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

class AliasWorkflow : StatefulWorkflow<Unit, AliasState, AliasOutput, AliasRendering>() {

    private val regex = Patterns.EMAIL_ADDRESS.toRegex()

    override fun initialState(props: Unit, snapshot: Snapshot?): AliasState =
        snapshot?.toParcelable() ?: AliasState()

    override fun snapshotState(state: AliasState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: AliasState,
        context: RenderContext,
    ): AliasRendering = AliasRendering(
        alias = renderState.alias,
        onAliasChange = context.send(::OnAliasChange),
        resolveEnabled = regex.matches(renderState.alias),
        onResolveClick = context.send(::OnResolveClick),
        onBackClick = context.send(::OnBackClick)
    )
}
