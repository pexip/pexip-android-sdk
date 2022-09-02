package com.pexip.sdk.sample.composer

import com.squareup.workflow1.WorkflowAction

typealias ComposerAction = WorkflowAction<Unit, ComposerState, ComposerOutput>

class OnMessageChange(private val message: String) : ComposerAction() {

    override fun Updater.apply() {
        state = ComposerState(message)
    }
}

class OnSubmitClick : ComposerAction() {

    override fun Updater.apply() {
        val message = requireNotNull(state.message.takeIf { it.isNotBlank() })
        state = ComposerState()
        setOutput(ComposerOutput.Submit(message.trim()))
    }
}
