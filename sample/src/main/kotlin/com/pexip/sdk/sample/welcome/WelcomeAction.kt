package com.pexip.sdk.sample.welcome

import com.squareup.workflow1.WorkflowAction

typealias WelcomeAction = WorkflowAction<Unit, WelcomeState, WelcomeOutput>

data class OnDisplayNameChange(val displayName: String) : WelcomeAction() {

    override fun Updater.apply() {
        state = state.copy(displayName = displayName)
    }
}

class OnDisplayNameSet : WelcomeAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = null)
        setOutput(WelcomeOutput.Next)
    }
}

class OnNextClick : WelcomeAction() {

    override fun Updater.apply() {
        state = state.copy(displayNameToSet = state.displayName)
    }
}

class OnBackClick : WelcomeAction() {

    override fun Updater.apply() {
        setOutput(WelcomeOutput.Back)
    }
}
