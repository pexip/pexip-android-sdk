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

import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.squareup.workflow1.WorkflowAction

typealias LocalMediaTrackAction = WorkflowAction<LocalMediaTrackProps, LocalMediaTrackState, Nothing>

class OnCapturingChange(private val capturing: Boolean) : LocalMediaTrackAction() {

    override fun Updater.apply() {
        val track = props.localMediaTrack
        when (capturing) {
            true -> when (track) {
                is LocalVideoTrack -> track.startCapture(QualityProfile.VeryHigh)
                else -> track.startCapture()
            }
            else -> track.stopCapture()
        }
    }
}

class OnCapturingStateChange(private val capturing: Boolean) : LocalMediaTrackAction() {

    override fun Updater.apply() {
        state = LocalMediaTrackState(capturing)
    }
}