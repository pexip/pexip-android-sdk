package com.pexip.sdk.sample.preflight

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

sealed interface PreflightDestination : Parcelable {

    @Parcelize
    object DisplayName : PreflightDestination

    @Parcelize
    object Alias : PreflightDestination

    @Parcelize
    data class PinChallenge(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val required: Boolean,
    ) : PreflightDestination
}
