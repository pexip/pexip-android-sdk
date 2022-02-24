package com.pexip.sdk.video.sample

import android.os.Parcelable
import com.pexip.sdk.video.Token
import kotlinx.parcelize.Parcelize

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val joinDetails: com.pexip.sdk.video.JoinDetails) : SampleState()

    @Parcelize
    data class PinRequirement(
        val joinDetails: com.pexip.sdk.video.JoinDetails,
        val node: com.pexip.sdk.video.Node,
    ) : SampleState()

    @Parcelize
    data class PinChallenge(
        val joinDetails: com.pexip.sdk.video.JoinDetails,
        val node: com.pexip.sdk.video.Node,
        val required: Boolean,
    ) : SampleState()

    @Parcelize
    data class Conference(val token: Token) : SampleState()
}
