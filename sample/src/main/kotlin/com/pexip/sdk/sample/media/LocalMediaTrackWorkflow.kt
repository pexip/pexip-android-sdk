/*
 * Copyright 2022-2025 Pexip AS
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

import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import javax.inject.Inject

private typealias LocalMediaTrackRenderContext =
    StatefulWorkflow.RenderContext<LocalMediaTrackProps, LocalMediaTrackState, Nothing>

class LocalMediaTrackWorkflow @Inject constructor() :
    StatefulWorkflow<
        LocalMediaTrackProps,
        LocalMediaTrackState,
        Nothing,
        LocalMediaTrackRendering,
        >() {

    override fun initialState(
        props: LocalMediaTrackProps,
        snapshot: Snapshot?,
    ): LocalMediaTrackState = LocalMediaTrackState(
        capturing = props.localMediaTrack.capturing,
        capturingWorker = props.localMediaTrack
            .getCapturing()
            .asWorker(),
    )

    override fun snapshotState(state: LocalMediaTrackState): Snapshot? = null

    override fun render(
        renderProps: LocalMediaTrackProps,
        renderState: LocalMediaTrackState,
        context: LocalMediaTrackRenderContext,
    ): LocalMediaTrackRendering {
        context.runningWorker(renderState.capturingWorker, handler = ::onCapturingStateChange)
        return LocalMediaTrackRendering(
            capturing = renderState.capturing,
            onCapturingChange = context.send(::onCapturingChange),
        )
    }

    override fun onPropsChanged(
        old: LocalMediaTrackProps,
        new: LocalMediaTrackProps,
        state: LocalMediaTrackState,
    ): LocalMediaTrackState = when (new) {
        old -> state
        else -> initialState(new, null)
    }

    private fun onCapturingChange(capturing: Boolean) =
        action({ "onCapturingChange($capturing)" }) {
            val track = props.localMediaTrack
            when (capturing) {
                true -> when (track) {
                    is LocalVideoTrack -> track.startCapture(QualityProfile.VeryHigh)
                    else -> track.startCapture()
                }
                else -> track.stopCapture()
            }
        }

    private fun onCapturingStateChange(capturing: Boolean) =
        action({ "onCapturingStateChange($capturing)" }) {
            state = state.copy(capturing = capturing)
        }
}
