package com.pexip.sdk.video.sample

import android.os.Parcelable
import com.pexip.sdk.api.infinity.RequestTokenResponse
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.net.URL

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val conferenceAlias: String, val host: String) : SampleState()

    @Parcelize
    data class PinRequirement(val node: URL, val conferenceAlias: String) : SampleState()

    @Parcelize
    data class PinChallenge(
        val node: URL,
        val conferenceAlias: String,
        val required: Boolean,
    ) : SampleState()

    @Parcelize
    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val response: @WriteWith<RequestTokenResponseParceler> RequestTokenResponse,
    ) : SampleState()
}
