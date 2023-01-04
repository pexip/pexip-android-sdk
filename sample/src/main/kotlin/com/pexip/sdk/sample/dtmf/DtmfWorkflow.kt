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
