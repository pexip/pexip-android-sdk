package com.pexip.sdk.sample.composer

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComposerWorkflow @Inject constructor() :
    StatefulWorkflow<Unit, ComposerState, ComposerOutput, ComposerRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): ComposerState =
        snapshot?.toParcelable() ?: ComposerState()

    override fun snapshotState(state: ComposerState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: ComposerState,
        context: RenderContext,
    ): ComposerRendering = ComposerRendering(
        message = renderState.message,
        submitEnabled = renderState.submitEnabled,
        onMessageChange = context.send(::OnMessageChange),
        onSubmitClick = context.send(::OnSubmitClick)
    )
}