package com.pexip.sdk.video.sample

import android.os.Parcelable
import com.pexip.sdk.video.token.Token
import kotlinx.parcelize.Parcelize

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val alias: String, val host: String) : SampleState()

    @Parcelize
    data class PinRequirement(
        val alias: String,
        val node: com.pexip.sdk.video.node.Node,
    ) : SampleState()

    @Parcelize
    data class PinChallenge(
        val alias: String,
        val node: com.pexip.sdk.video.node.Node,
        val required: Boolean,
    ) : SampleState()

    @Parcelize
    data class Conference(val token: Token) : SampleState()
}
