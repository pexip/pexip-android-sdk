package com.pexip.sdk.sample.dtmf

import android.media.ToneGenerator
import com.squareup.workflow1.WorkflowAction

typealias DtmfAction = WorkflowAction<Unit, DtmfState, DtmfOutput>

class OnToneClick(private val tone: String) : DtmfAction() {

    override fun Updater.apply() {
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
            else -> return
        }
        state.toneGenerator.startTone(toneType, 250)
        setOutput(DtmfOutput.Tone(tone))
    }
}

class OnBackClick : DtmfAction() {

    override fun Updater.apply() {
        setOutput(DtmfOutput.Back)
    }
}
