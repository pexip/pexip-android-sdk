package com.pexip.sdk.sample.welcome

sealed class WelcomeOutput {

    object Next : WelcomeOutput()
    object Back : WelcomeOutput()
}
