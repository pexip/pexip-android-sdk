package com.pexip.sdk.video.sample

import android.os.Parcelable
import android.util.Patterns
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

object SampleWorkflow : StatefulWorkflow<Unit, SampleState, SampleOutput, SampleRendering>() {

    private val regex = Patterns.EMAIL_ADDRESS.toRegex()

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState =
        snapshot?.toParcelable() ?: SampleState()

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: SampleState,
        context: RenderContext,
    ): SampleRendering = SampleRendering(
        value = renderState.value,
        onValueChange = context.eventHandler<String> {
            state = SampleState(it.trim())
        },
        resolveEnabled = regex.matches(renderState.value),
        onResolveClick = context.eventHandler {
            setOutput(SampleOutput(state.value))
        }
    )
}

@JvmInline
value class SampleOutput(val uri: String)

@Parcelize
@JvmInline
value class SampleState(val value: String = "") : Parcelable

data class SampleRendering(
    val value: String,
    val onValueChange: (String) -> Unit,
    val resolveEnabled: Boolean,
    val onResolveClick: () -> Unit,
)
