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
package com.pexip.sdk.sample.dtmf

import android.media.AudioManager
import android.media.ToneGenerator
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DtmfWorkflow @Inject constructor() :
    StatefulWorkflow<DtmfProps, DtmfState, DtmfOutput, DtmfRendering>() {

    override fun initialState(props: DtmfProps, snapshot: Snapshot?): DtmfState =
        DtmfState(ToneGenerator(AudioManager.STREAM_DTMF, 80))

    override fun snapshotState(state: DtmfState): Snapshot? = null

    override fun render(
        renderProps: DtmfProps,
        renderState: DtmfState,
        context: RenderContext,
    ): DtmfRendering {
        context.runningSideEffect(renderState.toString()) {
            try {
                awaitCancellation()
            } finally {
                with(renderState.toneGenerator) {
                    stopTone()
                    release()
                }
            }
        }
        return DtmfRendering(
            visible = renderProps.visible,
            onToneClick = context.send(::OnToneClick),
            onBackClick = context.send(::OnBackClick),
        )
    }
}
