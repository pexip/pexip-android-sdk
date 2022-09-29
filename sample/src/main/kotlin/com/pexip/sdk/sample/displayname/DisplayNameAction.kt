package com.pexip.sdk.sample.displayname

import com.squareup.workflow1.WorkflowAction

typealias DisplayNameAction = WorkflowAction<Unit, DisplayNameState, DisplayNameOutput>

data class OnDisplayNameChange(val displayName: String) : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayName = displayName)
    }
}

class OnDisplayNameSet : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = null)
        setOutput(DisplayNameOutput.Next)
    }
}

class OnNextClick : DisplayNameAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = state.displayName)
    }
}

class OnBackClick : DisplayNameAction() {

    override fun Updater.apply() {
        setOutput(DisplayNameOutput.Back)
    }
}
