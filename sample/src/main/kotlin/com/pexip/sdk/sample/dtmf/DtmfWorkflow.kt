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
package com.pexip.sdk.sample.dtmf

import android.media.AudioManager
import android.media.ToneGenerator
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import kotlinx.coroutines.awaitCancellation
import javax.inject.Inject
import javax.inject.Singleton

private typealias DtmfRenderContext =
    StatefulWorkflow.RenderContext<DtmfProps, DtmfState, DtmfOutput>

@Singleton
class DtmfWorkflow @Inject constructor() :
    StatefulWorkflow<DtmfProps, DtmfState, DtmfOutput, DtmfScreen>() {

    override fun initialState(props: DtmfProps, snapshot: Snapshot?): DtmfState =
        DtmfState(ToneGenerator(AudioManager.STREAM_DTMF, 80))

    override fun snapshotState(state: DtmfState): Snapshot? = null

    override fun render(
        renderProps: DtmfProps,
        renderState: DtmfState,
        context: DtmfRenderContext,
    ): DtmfScreen {
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
        return DtmfScreen(
            visible = renderProps.visible,
            onToneClick = context.send(::onToneClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun onToneClick(tone: String) = action({ "onToneClick($tone)" }) {
        val toneType = when (tone) {
            "0" -> ToneGenerator.TONE_DTMF_0
            "1" -> ToneGenerator.TONE_DTMF_1
            "2" -> ToneGenerator.TONE_DTMF_2
            "3" -> ToneGenerator.TONE_DTMF_3
            "4" -> ToneGenerator.TONE_DTMF_4
            "5" -> ToneGenerator.TONE_DTMF_5
            "6" -> ToneGenerator.TONE_DTMF_6
            "7" -> ToneGenerator.TONE_DTMF_7
            "8" -> ToneGenerator.TONE_DTMF_8
            "9" -> ToneGenerator.TONE_DTMF_9
            "*" -> ToneGenerator.TONE_DTMF_S
            "#" -> ToneGenerator.TONE_DTMF_P
            else -> return@action
        }
        state.toneGenerator.startTone(toneType, 250)
        setOutput(DtmfOutput.Tone(tone))
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(DtmfOutput.Back)
    }
}
