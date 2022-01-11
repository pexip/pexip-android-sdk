package com.pexip.sdk.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.pexip.sdk.video.node.NodeOutput
import com.pexip.sdk.video.node.NodeProps
import com.pexip.sdk.video.node.NodeWorkflow
import com.pexip.sdk.video.pin.PinChallengeProps
import com.pexip.sdk.video.pin.PinChallengeWorkflow
import com.pexip.sdk.video.pin.PinRequirementOutput
import com.pexip.sdk.video.pin.PinRequirementProps
import com.pexip.sdk.video.pin.PinRequirementWorkflow
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow

@ExperimentalWorkflowApi
class ConferenceWorkflow : Workflow<ConferenceProps, ConferenceOutput, Any> {

    private val nodeWorkflow = NodeWorkflow()
    private val pinRequirementWorkflow = PinRequirementWorkflow()
    private val pinChallengeWorkflow = PinChallengeWorkflow()

    private sealed class State {

        object Node : State()

        data class PinRequirement(val nodeAddress: String) : State()

        data class PinChallenge(val nodeAddress: String, val required: Boolean) : State()
    }

    @Composable
    override fun render(props: ConferenceProps, onOutput: (ConferenceOutput) -> Unit): Any {
        val (state, onStateChange) = remember { mutableStateOf<State>(State.Node) }
        return when (state) {
            is State.Node -> nodeWorkflow.render(NodeProps(props.uri)) {
                when (it) {
                    is NodeOutput.Node -> onStateChange(State.PinRequirement(it.address))
                    is NodeOutput.Back -> onOutput(ConferenceOutput.Finish)
                }
            }
            is State.PinRequirement -> pinRequirementWorkflow.render(
                props = PinRequirementProps(
                    nodeAddress = state.nodeAddress,
                    conferenceAlias = props.uri,
                    displayName = props.displayName
                ),
                onOutput = {
                    when (it) {
                        is PinRequirementOutput.Some -> {
                            onStateChange(State.PinChallenge(state.nodeAddress, it.required))
                        }
                        is PinRequirementOutput.None -> onOutput(ConferenceOutput.Finish)
                        is PinRequirementOutput.Back -> onOutput(ConferenceOutput.Finish)
                    }
                }
            )
            is State.PinChallenge -> pinChallengeWorkflow.render(
                props = PinChallengeProps(
                    nodeAddress = state.nodeAddress,
                    conferenceAlias = props.uri,
                    displayName = props.displayName,
                    required = state.required
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
