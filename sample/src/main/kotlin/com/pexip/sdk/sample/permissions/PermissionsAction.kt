package com.pexip.sdk.sample.permissions

import com.squareup.workflow1.WorkflowAction

typealias PermissionsAction = WorkflowAction<PermissionsProps, PermissionsState, PermissionsOutput>

class OnPermissionsRequestResult(private val result: PermissionsRequestResult) :
    PermissionsAction() {

    override fun Updater.apply() {
        val output = when {
            result.grants.all() -> PermissionsOutput.Next
            result.rationales.none() && state.rationales.none() -> PermissionsOutput.ApplicationDetailsSettings
            else -> null
        }
        output?.let(::setOutput)
        state = PermissionsState(result.rationales)
    }

    private fun <T> Map<T, Boolean>.all() = all { it.value }
    private fun <T> Map<T, Boolean>.none() = none { it.value }
}

class OnBackClick : PermissionsAction() {

    override fun Updater.apply() {
        setOutput(PermissionsOutput.Back)
    }
}
