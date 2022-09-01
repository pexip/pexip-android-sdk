package com.pexip.sdk.sample.composer

sealed class ComposerOutput {

    class Submit(val message: String) : ComposerOutput()
}
