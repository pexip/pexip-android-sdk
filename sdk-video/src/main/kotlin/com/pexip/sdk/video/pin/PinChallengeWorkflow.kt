package com.pexip.sdk.video.pin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Token
import com.pexip.sdk.video.api.internal.InvalidPinException
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

@ExperimentalWorkflowApi
class PinChallengeWorkflow(private val service: InfinityService) :
    Workflow<PinChallengeProps, PinChallengeOutput, PinChallengeRendering> {

    constructor() : this(InfinityService)

    private data class State(
        val pin: String = "",
        val t: Throwable? = null,
        val requesting: Boolean = false,
    )

    @Composable
    override fun render(
        props: PinChallengeProps,
        onOutput: (PinChallengeOutput) -> Unit,
    ): PinChallengeRendering {
        var state by remember { mutableStateOf(State()) }
        val submitFlow = rememberSubmitFlow()
        val currentOnOutput by rememberUpdatedState(onOutput)
        LaunchedEffect(props, submitFlow) {
            submitFlow.collectLatest {
                try {
                    state = state.copy(requesting = true)
                    val token = service.requestToken(
                        nodeAddress = props.nodeAddress,
                        conferenceAlias = props.conferenceAlias,
                        displayName = props.displayName,
                        pin = it.trim()
                    )
                    val output = PinChallengeOutput.Token(
                        token = token.token,
                        expires = token.expires
                    )
                    currentOnOutput(output)
                } catch (e: InvalidPinException) {
                    state = state.copy(pin = "", t = e, requesting = false)
                } catch (t: Throwable) {
                    state = state.copy(t = t, requesting = false)
                }
            }
        }
        return PinChallengeRendering(
            pin = state.pin,
            error = state.t != null,
            submitEnabled = when {
                state.requesting -> false
                props.required -> state.pin.isNotBlank()
                else -> false
            },
            onPinChange = { state = state.copy(pin = it, t = null) },
            onSubmitClick = { submitFlow.tryEmit(state.pin) },
            onBackClick = { onOutput(PinChallengeOutput.Back) }
        )
    }

    @Composable
    private fun rememberSubmitFlow() = remember {
        MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
}

@Immutable
class PinChallengeProps(
    val nodeAddress: String,
    val conferenceAlias: String,
    val displayName: String,
    val required: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PinChallengeProps) return false
        if (nodeAddress != other.nodeAddress) return false
        if (conferenceAlias != other.conferenceAlias) return false
        if (displayName != other.displayName) return false
        if (required != other.required) return false
        return true
    }

    override fun hashCode(): Int {
        var result = nodeAddress.hashCode()
        result = 31 * result + conferenceAlias.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + required.hashCode()
        return result
    }

    override fun toString(): String =
        "PinChallengeProps(nodeAddress=$nodeAddress, conferenceAlias=$conferenceAlias, displayName=$displayName, required=$required)"
}

@Immutable
sealed class PinChallengeOutput {

    @Immutable
    class Token(val token: String, val expires: Long) : PinChallengeOutput() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Token) return false
            if (token != other.token) return false
            if (expires != other.expires) return false
            return true
        }

        override fun hashCode(): Int {
            var result = token.hashCode()
            result = 31 * result + expires.hashCode()
            return result
        }

        override fun toString(): String = "Token(token=$token, expires=$expires)"
    }

    @Immutable
    object Back : PinChallengeOutput() {

        override fun toString(): String = "Back"
    }
}

@Immutable
class PinChallengeRendering(
    val pin: String,
    val error: Boolean,
    val submitEnabled: Boolean,
    val onPinChange: (String) -> Unit,
    val onSubmitClick: () -> Unit,
    val onBackClick: () -> Unit,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PinChallengeRendering) return false
        if (pin != other.pin) return false
        if (error != other.error) return false
        if (submitEnabled != other.submitEnabled) return false
        if (onPinChange != other.onPinChange) return false
        if (onSubmitClick != other.onSubmitClick) return false
        if (onBackClick != other.onBackClick) return false
        return true
    }

    override fun hashCode(): Int {
        var result = pin.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + submitEnabled.hashCode()
        result = 31 * result + onPinChange.hashCode()
        result = 31 * result + onSubmitClick.hashCode()
        result = 31 * result + onBackClick.hashCode()
        return result
    }

    override fun toString(): String =
        "PinChallengeRendering(pin=$pin, error=$error, submitEnabled=$submitEnabled, onPinChange=$onPinChange, onSubmitClick=$onSubmitClick, onBackClick=$onBackClick)"
}
