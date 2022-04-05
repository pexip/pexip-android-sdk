package com.pexip.sdk.video.sample.alias

import com.squareup.workflow1.WorkflowAction

typealias AliasAction = WorkflowAction<Unit, AliasState, AliasOutput>

data class OnAliasChange(val alias: String) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(alias = alias.trim())
    }
}

data class OnHostChange(val host: String) : AliasAction() {

    override fun Updater.apply() {
        state = state.copy(host = host.trim())
    }
}

class OnResolveClick : AliasAction() {

    override fun Updater.apply() {
        val output = AliasOutput.Alias(
            conferenceAlias = state.alias,
            host = when (val host = state.host) {
                "" -> state.alias.split("@").last()
                else -> host
            }
        )
        setOutput(output)
    }
}

class OnBackClick : AliasAction() {

    override fun Updater.apply() {
        setOutput(AliasOutput.Back)
    }
}
