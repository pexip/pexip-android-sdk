package com.pexip.sdk.sample.displayname

sealed class DisplayNameOutput {

    object Next : DisplayNameOutput() {

        override fun toString(): String = "Next"
    }

    object Back : DisplayNameOutput() {

        override fun toString(): String = "Back"
    }
}
