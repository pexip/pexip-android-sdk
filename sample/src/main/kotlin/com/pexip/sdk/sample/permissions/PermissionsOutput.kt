package com.pexip.sdk.sample.permissions

sealed class PermissionsOutput {

    object ApplicationDetailsSettings : PermissionsOutput() {

        override fun toString(): String = "ApplicationDetailsSettings"
    }

    object Next : PermissionsOutput() {

        override fun toString(): String = "Next"
    }

    object Back : PermissionsOutput() {

        override fun toString(): String = "Back"
    }
}
