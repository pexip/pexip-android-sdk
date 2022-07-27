package com.pexip.sdk.sample.welcome

import com.pexip.sdk.sample.send
import com.pexip.sdk.sample.settings.SettingsStore
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WelcomeWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<Unit, WelcomeState, WelcomeOutput, WelcomeRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): WelcomeState =
        snapshot?.toParcelable() ?: WelcomeState()

    override fun snapshotState(state: WelcomeState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: WelcomeState,
        context: RenderContext,
    ): WelcomeRendering {
        context.getDisplayNameSideEffect()
        context.setDisplayNameSideEffect(renderState.displayNameToSet)
        return WelcomeRendering(
            displayName = renderState.displayName,
            onDisplayNameChange = context.send(::OnDisplayNameChange),
            onNextClick = context.send(::OnNextClick),
            onBackClick = context.send(::OnBackClick)
        )
    }

    private fun RenderContext.getDisplayNameSideEffect() = runningSideEffect("getDisplayName") {
        val displayName = store.getDisplayName().first()
        val action = OnDisplayNameChange(displayName)
        actionSink.send(action)
    }

    private fun RenderContext.setDisplayNameSideEffect(displayNameToSet: String?) {
        val displayName = (displayNameToSet?.takeIf { it.isNotBlank() } ?: return).trim()
        runningSideEffect("setDisplayName($displayName)") {
            store.setDisplayName(displayName)
            val action = OnDisplayNameSet()
            actionSink.send(action)
        }
    }
}
