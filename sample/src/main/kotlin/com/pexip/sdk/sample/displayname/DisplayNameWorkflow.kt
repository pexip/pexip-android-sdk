package com.pexip.sdk.sample.displayname

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
class DisplayNameWorkflow @Inject constructor(private val store: SettingsStore) :
    StatefulWorkflow<Unit, DisplayNameState, DisplayNameOutput, DisplayNameRendering>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): DisplayNameState =
        snapshot?.toParcelable() ?: DisplayNameState()

    override fun snapshotState(state: DisplayNameState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: DisplayNameState,
        context: RenderContext,
    ): DisplayNameRendering {
        context.getDisplayNameSideEffect()
        context.setDisplayNameSideEffect(renderState.displayNameToSet)
        return DisplayNameRendering(
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
