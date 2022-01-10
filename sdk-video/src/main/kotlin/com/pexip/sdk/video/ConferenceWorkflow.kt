package com.pexip.sdk.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.pexip.sdk.video.node.NodeOutput
import com.pexip.sdk.video.node.NodeProps
import com.pexip.sdk.video.node.NodeWorkflow
import com.pexip.sdk.video.pin.PinRequirementProps
import com.pexip.sdk.video.pin.PinRequirementWorkflow
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow

@ExperimentalWorkflowApi
class ConferenceWorkflow : Workflow<ConferenceProps, ConferenceOutput, Any> {

    private val nodeWorkflow = NodeWorkflow()
    private val pinRequirementWorkflow = PinRequirementWorkflow()

    private sealed class State {

        object Node : State()

        data class PinRequirement(
            val nodeAddress: String,
            val conferenceAlias: String,
            val displayName: String,
        ) : State()
    }

    @Composable
    override fun render(props: ConferenceProps, onOutput: (ConferenceOutput) -> Unit): Any {
        val (state, onStateChange) = remember { mutableStateOf<State>(State.Node) }
        return when (state) {
            is State.Node -> nodeWorkflow.render(NodeProps(props.uri)) {
                when (it) {
                    is NodeOutput.Node -> {
                        val s = State.PinRequirement(
                            nodeAddress = it.address,
                            conferenceAlias = props.uri,
                            displayName = props.displayName
                        )
                        onStateChange(s)
                    }
                    is NodeOutput.Back -> onOutput(ConferenceOutput.Finish)
                }
            }
            is State.PinRequirement -> pinRequirementWorkflow.render(
                props = PinRequirementProps(
                    nodeAddress = state.nodeAddress,
                    conferenceAlias = state.conferenceAlias,
                    displayName = state.displayName
                ),
                onOutput = {
                    onOutput(ConferenceOutput.Finish)
                }
            )
        }
    }
}

sealed class ConferenceOutput {

    object Finish : ConferenceOutput()
}
