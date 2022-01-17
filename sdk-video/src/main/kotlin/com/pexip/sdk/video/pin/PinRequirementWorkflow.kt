package com.pexip.sdk.video.pin

import android.os.Parcelable
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.PinRequirement
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class PinRequirementWorkflow(private val service: InfinityService) :
    StatefulWorkflow<PinRequirementProps, PinRequirementState, PinRequirementOutput, PinRequirementRendering>() {

    constructor() : this(InfinityService)

    override fun initialState(
        props: PinRequirementProps,
        snapshot: Snapshot?,
    ): PinRequirementState = snapshot?.toParcelable() ?: PinRequirementState.ResolvingPinRequirement

    override fun snapshotState(state: PinRequirementState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: PinRequirementProps,
        renderState: PinRequirementState,
        context: RenderContext,
    ): PinRequirementRendering {
        context.getPinRequirementSideEffect(renderProps)
        return when (renderState) {
            is PinRequirementState.ResolvingPinRequirement -> PinRequirementRendering.ResolvingPinRequirement
            is PinRequirementState.Failure -> PinRequirementRendering.Failure(
                t = renderState.t,
                onBackClick = context.eventHandler({ "OnBackClick" }) {
                    setOutput(PinRequirementOutput.Back)
                }
            )
        }
    }

    private fun RenderContext.getPinRequirementSideEffect(props: PinRequirementProps) =
        runningSideEffect(props.toString()) {
            val action = try {
                val pinRequirement = service.getPinRequirement(
                    nodeAddress = props.nodeAddress,
                    conferenceAlias = props.conferenceAlias,
                    displayName = props.displayName
                )
                onPinRequirement(pinRequirement)
            } catch (t: Throwable) {
                onError(t)
            }
            actionSink.send(action)
        }

    private fun onPinRequirement(pinRequirement: PinRequirement) =
        action({ "OnPinRequirement($pinRequirement)" }) {
            val output = when (pinRequirement) {
                is PinRequirement.None -> PinRequirementOutput.None(
                    token = pinRequirement.token,
                    expires = pinRequirement.expires
                )
                is PinRequirement.Some -> PinRequirementOutput.Some(pinRequirement.required)
            }
            setOutput(output)
        }

    private fun onError(t: Throwable) = action({ "OnError($t)" }) {
        state = PinRequirementState.Failure(t)
    }
}

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

sealed class PinRequirementState : Parcelable {

    @Parcelize
    object ResolvingPinRequirement : PinRequirementState()

    @Parcelize
    data class Failure(val t: Throwable) : PinRequirementState()
}

sealed class PinRequirementOutput {

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

    object Back : PinRequirementOutput() {

        override fun toString(): String = "Back"
    }
}

sealed class PinRequirementRendering {

    object ResolvingPinRequirement : PinRequirementRendering() {

        override fun toString(): String = "ResolvingPinRequirement"
    }

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
