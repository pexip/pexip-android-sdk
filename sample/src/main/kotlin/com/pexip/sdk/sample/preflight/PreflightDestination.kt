package com.pexip.sdk.sample.preflight

import java.net.URL

sealed interface PreflightDestination {

    object DisplayName : PreflightDestination

    object Alias : PreflightDestination

    data class PinChallenge(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val required: Boolean,
    ) : PreflightDestination
}
