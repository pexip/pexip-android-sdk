package com.pexip.sdk.video.sample.alias

sealed class AliasOutput {

    data class Alias(val conferenceAlias: String, val host: String) : AliasOutput()
    object Back : AliasOutput()
}
