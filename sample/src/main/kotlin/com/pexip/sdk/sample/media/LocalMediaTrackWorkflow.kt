package com.pexip.sdk.sample.media

import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMediaTrackWorkflow @Inject constructor() :
    StatefulWorkflow<LocalMediaTrackProps, LocalMediaTrackState, Nothing, LocalMediaTrackRendering>() {

    override fun initialState(
        props: LocalMediaTrackProps,
        snapshot: Snapshot?,
    ): LocalMediaTrackState = LocalMediaTrackState(props.localMediaTrack.capturing)

    override fun snapshotState(state: LocalMediaTrackState): Snapshot? = null

    override fun render(
        renderProps: LocalMediaTrackProps,
        renderState: LocalMediaTrackState,
        context: RenderContext,
    ): LocalMediaTrackRendering {
        context.capturingSideEffect(renderProps.localMediaTrack)
        return LocalMediaTrackRendering(
            capturing = renderState.capturing,
            onCapturingChange = context.send(::OnCapturingChange)
        )
    }

    override fun onPropsChanged(
        old: LocalMediaTrackProps,
        new: LocalMediaTrackProps,
        state: LocalMediaTrackState,
    ): LocalMediaTrackState = when (new) {
        old -> state
        else -> LocalMediaTrackState(new.localMediaTrack.capturing)
    }

    private fun RenderContext.capturingSideEffect(track: LocalMediaTrack) =
        runningSideEffect("capturingSideEffect($track)") {
            track.getCapturing()
                .map(::OnCapturingStateChange)
                .collectLatest(actionSink::send)
        }
}
