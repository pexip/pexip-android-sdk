package com.pexip.sdk.sample.pinchallenge

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinChallengeWorkflow @Inject constructor(
    private val store: SettingsStore,
    private val service: InfinityService,
) : StatefulWorkflow<PinChallengeProps, PinChallengeState, PinChallengeOutput, PinChallengeRendering>() {

    override fun initialState(props: PinChallengeProps, snapshot: Snapshot?): PinChallengeState =
        PinChallengeState()

    override fun snapshotState(state: PinChallengeState): Snapshot? = null

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
                val displayName = store.getDisplayName().first()
                val request = RequestTokenRequest(displayName = displayName)
                val response = service.newRequest(props.node)
                    .conference(props.conferenceAlias)
                    .requestToken(request, pinToSubmit)
                    .await()
                OnResponse(response)
            } catch (e: InvalidPinException) {
                OnInvalidPin(e)
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
    }
}
