package com.pexip.sdk.video.sample.alias

import com.pexip.sdk.video.api.ConferenceAlias

sealed class AliasOutput {

    data class Alias(val conferenceAlias: ConferenceAlias, val host: String) : AliasOutput()
    object Back : AliasOutput()
}
