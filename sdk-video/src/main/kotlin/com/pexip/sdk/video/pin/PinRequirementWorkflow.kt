package com.pexip.sdk.video.pin

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.PinRequirement
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow

@ExperimentalWorkflowApi
class PinRequirementWorkflow(private val service: InfinityService) :
    Workflow<PinRequirementProps, PinRequirementOutput, PinRequirementRendering> {

    constructor() : this(InfinityService)

    private sealed class State {
        object ResolvingPinRequirement : State()
        data class Failure(val t: Throwable) : State()
    }

    @SuppressLint("ComposableNaming")
    @Composable
    override fun render(
        props: PinRequirementProps,
        onOutput: (PinRequirementOutput) -> Unit,
    ): PinRequirementRendering {
        val (state, onStateChange) = remember { mutableStateOf<State>(State.ResolvingPinRequirement) }
        val currentOnOutput by rememberUpdatedState(onOutput)
        LaunchedEffect(props) {
            try {
                val pinRequirement = service.getPinRequirement(
                    nodeAddress = props.nodeAddress,
                    conferenceAlias = props.conferenceAlias,
                    displayName = props.displayName
                )
                val output = when (pinRequirement) {
                    is PinRequirement.None -> PinRequirementOutput.None(
                        token = pinRequirement.token,
                        expires = pinRequirement.expires
                    )
                    is PinRequirement.Some -> PinRequirementOutput.Some(pinRequirement.required)
                }
                currentOnOutput(output)
            } catch (t: Throwable) {
                onStateChange(State.Failure(t))
            }
        }
        return when (state) {
            is State.ResolvingPinRequirement -> PinRequirementRendering.ResolvingPinRequirement
            is State.Failure -> PinRequirementRendering.Failure(
                t = state.t,
                onBackClick = { onOutput(PinRequirementOutput.Back) }
            )
        }
    }
}

@Immutable
class PinRequirementProps(
    val nodeAddress: String,
    val conferenceAlias: String,
    val displayName: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PinRequirementProps) return false
        if (nodeAddress != other.nodeAddress) return false
        if (conferenceAlias != other.conferenceAlias) return false
        if (displayName != other.displayName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = nodeAddress.hashCode()
        result = 31 * result + conferenceAlias.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }

    override fun toString(): String =
        "PinRequirementProps(nodeAddress=$nodeAddress, conferenceAlias=$conferenceAlias, displayName=$displayName)"
}

@Immutable
sealed class PinRequirementOutput {

    @Immutable
    class Some(val required: Boolean) : PinRequirementOutput() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Some) return false
            if (required != other.required) return false
            return true
        }

        override fun hashCode(): Int = required.hashCode()

        override fun toString(): String = "Some(required=$required)"
    }

    @Immutable
    class None(val token: String, val expires: Long) : PinRequirementOutput() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is None) return false
            if (token != other.token) return false
            if (expires != other.expires) return false
            return true
        }

        override fun hashCode(): Int {
            var result = token.hashCode()
            result = 31 * result + expires.hashCode()
            return result
        }

        override fun toString(): String = "None(token=$token, expires=$expires)"
    }

    @Immutable
    object Back : PinRequirementOutput() {

        override fun toString(): String = "Back"
    }
}

@Immutable
sealed class PinRequirementRendering {

    @Immutable
    object ResolvingPinRequirement : PinRequirementRendering() {

        override fun toString(): String = "ResolvingPinRequirement"
    }

    @Immutable
    class Failure(val t: Throwable, val onBackClick: () -> Unit) : PinRequirementRendering() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Failure) return false
            if (t != other.t) return false
            if (onBackClick != other.onBackClick) return false
            return true
        }

        override fun hashCode(): Int {
            var result = t.hashCode()
            result = 31 * result + onBackClick.hashCode()
            return result
        }

        override fun toString(): String = "Failure(t=$t, onBackClick=$onBackClick)"
    }
}
