package com.pexip.sdk.video.sample.alias

sealed class AliasOutput {

    data class Alias(val alias: String) : AliasOutput()
    object Back : AliasOutput()
}
