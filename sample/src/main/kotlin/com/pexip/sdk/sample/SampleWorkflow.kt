package com.pexip.sdk.sample

import android.Manifest
import android.content.Context
import android.os.Build
import com.pexip.sdk.sample.alias.AliasWorkflow
import com.pexip.sdk.sample.conference.ConferenceProps
import com.pexip.sdk.sample.conference.ConferenceWorkflow
import com.pexip.sdk.sample.displayname.DisplayNameWorkflow
import com.pexip.sdk.sample.permissions.PermissionsProps
import com.pexip.sdk.sample.permissions.PermissionsWorkflow
import com.pexip.sdk.sample.pinchallenge.PinChallengeProps
import com.pexip.sdk.sample.pinchallenge.PinChallengeWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleWorkflow @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionsWorkflow: PermissionsWorkflow,
    private val displayNameWorkflow: DisplayNameWorkflow,
    private val aliasWorkflow: AliasWorkflow,
    private val pinChallengeWorkflow: PinChallengeWorkflow,
    private val conferenceWorkflow: ConferenceWorkflow,
) : StatefulWorkflow<Unit, SampleState, SampleOutput, Any>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState {
        if (Permissions.all(context::isPermissionGranted)) {
            return snapshot?.toParcelable() ?: SampleState.DisplayName
        }
        return SampleState.Permissions
    }

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: SampleState,
        context: RenderContext,
    ): Any = when (renderState) {
        is SampleState.Permissions -> context.renderChild(
            child = permissionsWorkflow,
            props = PermissionsProps(Permissions),
            handler = ::OnPermissionsOutput
        )
        is SampleState.DisplayName -> context.renderChild(
            child = displayNameWorkflow,
            handler = ::OnDisplayNameOutput
        )
        is SampleState.Alias -> context.renderChild(
            child = aliasWorkflow,
            handler = ::OnAliasOutput
        )
        is SampleState.PinChallenge -> context.renderChild(
            child = pinChallengeWorkflow,
            props = PinChallengeProps(
                node = renderState.node,
                conferenceAlias = renderState.conferenceAlias,
                required = renderState.required
            ),
            handler = ::OnPinChallengeOutput
        )
        is SampleState.Conference -> context.renderChild(
            child = conferenceWorkflow,
            props = ConferenceProps(
                node = renderState.node,
                conferenceAlias = renderState.conferenceAlias,
                presentationInMain = renderState.presentationInMain,
                response = renderState.response
            ),
            handler = ::OnConferenceOutput
        )
    }

    companion object {

        private val Permissions = buildSet {
            add(Manifest.permission.RECORD_AUDIO)
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}
