package com.pexip.sdk.video.sample

import android.os.Parcelable
import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.RequestTokenResponse
import kotlinx.parcelize.Parcelize

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val conferenceAlias: ConferenceAlias, val host: String) : SampleState()

    @Parcelize
    data class PinRequirement(
        val node: com.pexip.sdk.video.api.Node,
        val conferenceAlias: ConferenceAlias,
    ) : SampleState()

    @Parcelize
    data class PinChallenge(
        val node: com.pexip.sdk.video.api.Node,
        val conferenceAlias: ConferenceAlias,
        val required: Boolean,
    ) : SampleState()

    @Parcelize
    data class Conference(
        val node: com.pexip.sdk.video.api.Node,
        val conferenceAlias: ConferenceAlias,
        val response: RequestTokenResponse,
    ) : SampleState()
}
