package com.pexip.sdk.video.sample

import android.os.Parcelable
import com.pexip.sdk.video.ConferenceOutput
import com.pexip.sdk.video.ConferenceProps
import com.pexip.sdk.video.ConferenceWorkflow
import com.pexip.sdk.video.sample.alias.AliasOutput
import com.pexip.sdk.video.sample.alias.AliasWorkflow
import com.pexip.sdk.video.sample.node.NodeOutput
import com.pexip.sdk.video.sample.node.NodeProps
import com.pexip.sdk.video.sample.node.NodeWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

object SampleWorkflow : StatefulWorkflow<Unit, SampleState, SampleOutput, Any>() {

    private val AliasWorkflow = AliasWorkflow()
    private val NodeWorkflow = NodeWorkflow()
    private val ConferenceWorkflow = ConferenceWorkflow()

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState =
        snapshot?.toParcelable() ?: SampleState.Alias

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: SampleState,
        context: RenderContext,
    ): Any = when (renderState) {
        is SampleState.Alias -> context.renderChild(
            child = AliasWorkflow,
            handler = ::onAliasOutput
        )
        is SampleState.Node -> context.renderChild(
            child = NodeWorkflow,
            props = NodeProps(renderState.host),
            handler = ::onNodeOutput
        )
        is SampleState.Conference -> context.renderChild(
            child = ConferenceWorkflow,
            props = ConferenceProps {
                alias(renderState.alias)
                nodeAddress(renderState.nodeAddress)
                displayName("Pexip Video SDK")
            },
            handler = ::onConferenceOutput
        )
    }

    private fun onAliasOutput(output: AliasOutput) = action({ "OnAliasOutput($output)" }) {
        when (output) {
            is AliasOutput.Alias -> state = SampleState.Node(
                alias = output.alias,
                host = output.alias.split("@").last()
            )
            is AliasOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }

    private fun onNodeOutput(output: NodeOutput) = action({ "OnNodeOutput($output)" }) {
        val s = checkNotNull(state as? SampleState.Node) { "Invalid state: $state" }
        when (output) {
            is NodeOutput.Node -> state = SampleState.Conference(s.alias, output.address)
            is NodeOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }

    private fun onConferenceOutput(output: ConferenceOutput) =
        action({ "OnConferenceOutput($output)" }) {
            when (output) {
                is ConferenceOutput.Finish -> setOutput(SampleOutput.Finish)
            }
        }
}

sealed class SampleOutput {

    object Finish : SampleOutput()
}

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val alias: String, val host: String) : SampleState()

    @Parcelize
    data class Conference(val alias: String, val nodeAddress: String) : SampleState()
}
