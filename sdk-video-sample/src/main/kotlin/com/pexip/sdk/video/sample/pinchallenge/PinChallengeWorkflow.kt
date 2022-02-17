package com.pexip.sdk.video.sample.pinchallenge

import com.pexip.sdk.video.InvalidPinException
import com.pexip.sdk.video.TokenRequest
import com.pexip.sdk.video.TokenRequester
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

class PinChallengeWorkflow(private val requester: TokenRequester) :
    StatefulWorkflow<PinChallengeProps, PinChallengeState, PinChallengeOutput, PinChallengeRendering>() {

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
                else -> true
            },
            onPinChange = context.send(::OnPinChange),
            onSubmitClick = context.send(::OnSubmitClick),
            onBackClick = context.send(::OnBackClick)
        )
    }

    private fun RenderContext.verifyPinSideEffect(
        props: PinChallengeProps,
        state: PinChallengeState,
    ) {
        val pinToSubmit = state.pinToSubmit ?: return
        runningSideEffect("$props:$pinToSubmit") {
            actionSink.send(OnRequestToken())
            val action = try {
                val request = TokenRequest.Builder()
                    .nodeAddress(props.nodeAddress)
                    .alias(props.alias)
                    .displayName(props.displayName)
                    .pin(pinToSubmit)
                    .build()
                val token = requester.requestToken(request)
                OnToken(token)
            } catch (e: InvalidPinException) {
                OnInvalidPin(e)
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
    }
}
