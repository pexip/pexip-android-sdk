package com.pexip.sdk.sample.composer

@JvmInline
value class ComposerState(val message: String = "") {

    val submitEnabled: Boolean
        get() = message.isNotBlank()
}
