package com.pexip.sdk.video.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val alias: String, val host: String) : SampleState()

    @Parcelize
    data class PinRequirement(val alias: String, val nodeAddress: String) : SampleState()

    @Parcelize
    data class PinChallenge(val alias: String, val nodeAddress: String, val required: Boolean) :
        SampleState()
}
