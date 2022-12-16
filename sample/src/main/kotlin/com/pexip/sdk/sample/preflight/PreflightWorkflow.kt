package com.pexip.sdk.sample.preflight

import com.pexip.sdk.sample.alias.AliasWorkflow
import com.pexip.sdk.sample.displayname.DisplayNameWorkflow
import com.pexip.sdk.sample.media.LocalMediaTrackProps
import com.pexip.sdk.sample.media.LocalMediaTrackWorkflow
import com.pexip.sdk.sample.pinchallenge.PinChallengeProps
import com.pexip.sdk.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
import javax.inject.Inject

class PreflightWorkflow @Inject constructor(
    private val displayNameWorkflow: DisplayNameWorkflow,
    private val aliasWorkflow: AliasWorkflow,
    private val pinChallengeWorkflow: PinChallengeWorkflow,
    private val localMediaTrackWorkflow: LocalMediaTrackWorkflow,
) : StatefulWorkflow<PreflightProps, PreflightState, PreflightOutput, PreflightRendering>() {

    override fun initialState(props: PreflightProps, snapshot: Snapshot?): PreflightState =
        PreflightState()

    override fun snapshotState(state: PreflightState): Snapshot? = null

    override fun render(
        renderProps: PreflightProps,
        renderState: PreflightState,
        context: RenderContext,
    ): PreflightRendering = PreflightRendering(
        childRendering = when (val destination = renderState.destination) {
            is PreflightDestination.DisplayName -> context.renderChild(
                child = displayNameWorkflow,
                handler = ::OnDisplayNameOutput
            )
            is PreflightDestination.Alias -> context.renderChild(
                child = aliasWorkflow,
                handler = ::OnAliasOutput
            )
            is PreflightDestination.PinChallenge -> context.renderChild(
                child = pinChallengeWorkflow,
                props = PinChallengeProps(
                    node = destination.node,
                    conferenceAlias = destination.conferenceAlias,
                    required = destination.required
                ),
                handler = ::OnPinChallengeOutput
            )
            null -> null
        },
        cameraVideoTrack = renderProps.cameraVideoTrack,
        callEnabled = with(renderProps) { cameraVideoTrack != null && microphoneAudioTrack != null },
        onCallClick = context.send(::OnCallClick),
        onCreateCameraVideoTrackClick = context.send(::OnCreateCameraVideoTrackClick),
        cameraVideoTrackRendering = when (renderProps.cameraVideoTrack) {
            null -> null
            else -> context.renderChild(
                child = localMediaTrackWorkflow,
                key = "cameraVideoTrack",
                props = LocalMediaTrackProps(renderProps.cameraVideoTrack)
            )
        },
        microphoneAudioTrackRendering = when (renderProps.microphoneAudioTrack) {
            null -> null
            else -> context.renderChild(
                child = localMediaTrackWorkflow,
                key = "microphoneAudioTrack",
                props = LocalMediaTrackProps(renderProps.microphoneAudioTrack)
            )
        },
        onBackClick = context.send(::OnBackClick)
    )
}
