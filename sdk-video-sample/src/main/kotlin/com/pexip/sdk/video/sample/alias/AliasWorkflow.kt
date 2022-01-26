package com.pexip.sdk.video.sample.alias

import android.os.Parcelable
import android.util.Patterns
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

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
        onAliasChange = context.eventHandler<String> {
            state = AliasState(it.trim())
        },
        resolveEnabled = regex.matches(renderState.alias),
        onResolveClick = context.eventHandler {
            setOutput(AliasOutput.Alias(renderState.alias))
        },
        onBackClick = context.eventHandler {
            setOutput(AliasOutput.Back)
        }
    )
}

sealed class AliasOutput {

    data class Alias(val alias: String) : AliasOutput()
    object Back : AliasOutput()
}

@Parcelize
@JvmInline
value class AliasState(val alias: String = "") : Parcelable

data class AliasRendering(
    val alias: String,
    val onAliasChange: (String) -> Unit,
    val resolveEnabled: Boolean,
    val onResolveClick: () -> Unit,
    val onBackClick: () -> Unit,
)
