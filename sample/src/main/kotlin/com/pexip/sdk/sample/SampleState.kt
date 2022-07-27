package com.pexip.sdk.sample

import android.os.Parcelable
import com.pexip.sdk.api.infinity.RequestTokenResponse
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.net.URL

sealed class SampleState : Parcelable {

    @Parcelize
    object Welcome : SampleState()

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class PinRequirement(
        val conferenceAlias: String,
        val host: String,
        val presentationInMain: Boolean,
    ) : SampleState()

    @Parcelize
    data class PinChallenge(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val required: Boolean,
    ) : SampleState()

    @Parcelize
    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val response: @WriteWith<RequestTokenResponseParceler> RequestTokenResponse,
    ) : SampleState()
}
