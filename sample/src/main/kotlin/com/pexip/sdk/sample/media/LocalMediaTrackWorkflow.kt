/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            onCapturingChange = context.send(::OnCapturingChange),
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
