package com.pexip.sdk.video.pin

import android.os.Parcelable
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Token
import com.pexip.sdk.video.api.internal.InvalidPinException
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class PinChallengeWorkflow(private val service: InfinityService) :
    StatefulWorkflow<PinChallengeProps, PinChallengeState, PinChallengeOutput, PinChallengeRendering>() {

    constructor() : this(InfinityService)

    override fun initialState(props: PinChallengeProps, snapshot: Snapshot?): PinChallengeState =
        snapshot?.toParcelable() ?: PinChallengeState()

    override fun snapshotState(state: PinChallengeState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: PinChallengeProps,
        renderState: PinChallengeState,
        context: RenderContext,
    ): PinChallengeRendering {
        context.verifyPinSideEffect(renderProps, renderState)
        return PinChallengeRendering(
            pin = renderState.pin,
            error = renderState.t != null,
            submitEnabled = when {
                renderState.requesting -> false
                renderProps.required -> renderState.pin.isNotBlank()
                else -> false
            },
            onPinChange = context.eventHandler<String>({ "OnPinChange" }) {
                state = state.copy(pin = it.trim(), t = null)
            },
            onSubmitClick = context.eventHandler({ "OnSubmitClick" }) {
                state = state.copy(pinToSubmit = state.pin)
            },
            onBackClick = context.eventHandler({ "OnBackClick" }) {
                setOutput(PinChallengeOutput.Back)
            }
        )
    }

    private fun RenderContext.verifyPinSideEffect(
        props: PinChallengeProps,
        state: PinChallengeState,
    ) {
        val pinToSubmit = state.pinToSubmit ?: return
        runningSideEffect("$props:$pinToSubmit") {
            actionSink.send(onRequestToken())
            val action = try {
                val token = service.requestToken(
                    nodeAddress = props.nodeAddress,
                    conferenceAlias = props.conferenceAlias,
                    displayName = props.displayName,
                    pin = pinToSubmit
                )
                onToken(token)
            } catch (e: InvalidPinException) {
                onInvalidPin(e)
            } catch (t: Throwable) {
                onError(t)
            }
            actionSink.send(action)
        }
    }

    private fun onRequestToken() = action({ "OnRequestToken" }) {
        state = state.copy(requesting = true)
    }

    private fun onToken(token: Token) = action({ "OnToken($token)" }) {
        val output = PinChallengeOutput.Token(
            token = token.token,
            expires = token.expires
        )
        setOutput(output)
    }

    private fun onInvalidPin(e: InvalidPinException) = action({ "OnInvalidPin($e)" }) {
        state = state.copy(
            pin = "",
            t = e,
            requesting = false,
            pinToSubmit = null
        )
    }

    private fun onError(t: Throwable) = action({ "OnError($t)" }) {
        state = state.copy(
            t = t,
            requesting = false,
            pinToSubmit = null
        )
    }
}

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

@Parcelize
data class PinChallengeState(
    val pin: String = "",
    val t: Throwable? = null,
    val requesting: Boolean = false,
    val pinToSubmit: String? = null,
) : Parcelable

sealed class PinChallengeOutput {

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

    object Back : PinChallengeOutput() {

        override fun toString(): String = "Back"
    }
}

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
