package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.Conference
import com.pexip.sdk.video.coroutines.localVideoTrack
import com.pexip.sdk.video.coroutines.remoteVideoTrack
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient

class ConferenceWorkflow(private val client: OkHttpClient) :
    StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, ConferenceRendering>() {

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState =
        Conference.Builder()
            .token(props.token)
            .client(client)
            .build()
            .let(::ConferenceState)

    override fun snapshotState(state: ConferenceState): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): ConferenceRendering {
        context.leaveSideEffect(renderState)
        context.localVideoTrackSideEffect(renderState)
        context.remoteVideoTrackSideEffect(renderState)
        return ConferenceRendering(
            localVideoTrack = renderState.localVideoTrack,
            remoteVideoTrack = renderState.remoteVideoTrack,
            onBackClick = context.send(::OnBackClick)
        )
    }

    private fun RenderContext.leaveSideEffect(renderState: ConferenceState) {
        runningSideEffect("${renderState.conference}Leave") {
            try {
                awaitCancellation()
            } finally {
                renderState.conference.leave()
            }
        }
    }

    private fun RenderContext.localVideoTrackSideEffect(renderState: ConferenceState) {
        runningSideEffect("${renderState.conference}LocalVideoTrack") {
            renderState.conference.callHandler.localVideoTrack()
                .map(::OnLocalVideoTrack)
                .collect(actionSink::send)
        }
    }

    private fun RenderContext.remoteVideoTrackSideEffect(renderState: ConferenceState) {
        runningSideEffect("${renderState.conference}RemoteVideoTrack") {
            renderState.conference.callHandler.remoteVideoTrack()
                .map(::OnRemoteVideoTrack)
                .collect(actionSink::send)
        }
    }
}
