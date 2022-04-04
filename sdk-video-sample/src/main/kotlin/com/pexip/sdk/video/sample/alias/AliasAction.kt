package com.pexip.sdk.video.sample.alias

import com.squareup.workflow1.WorkflowAction

typealias AliasAction = WorkflowAction<Unit, AliasState, AliasOutput>

data class OnAliasChange(val alias: String) : AliasAction() {

    override fun Updater.apply() {
        state = AliasState(alias.trim())
    }
}

class OnResolveClick : AliasAction() {

    override fun Updater.apply() {
        val output = AliasOutput.Alias(
            conferenceAlias = state.conferenceAlias,
            host = state.conferenceAlias.split("@").last()
        )
        setOutput(output)
    }
}

class OnBackClick : AliasAction() {

    override fun Updater.apply() {
        setOutput(AliasOutput.Back)
    }
}
