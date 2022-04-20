package com.pexip.sdk.video.sample.alias

sealed class AliasOutput {

    data class Alias(
        val conferenceAlias: String,
        val host: String,
        val presentationInMain: Boolean,
    ) : AliasOutput()

    object Back : AliasOutput()
}
