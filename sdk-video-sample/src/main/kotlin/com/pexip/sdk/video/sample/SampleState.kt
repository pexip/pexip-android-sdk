package com.pexip.sdk.video.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import okhttp3.HttpUrl

sealed class SampleState : Parcelable {

    @Parcelize
    object Alias : SampleState()

    @Parcelize
    data class Node(val alias: String, val host: String) : SampleState()

    @Parcelize
    data class PinRequirement(
        val alias: String,
        val nodeAddress: @WriteWith<HttpUrlParceler> HttpUrl,
    ) : SampleState()

    @Parcelize
    data class PinChallenge(
        val alias: String,
        val nodeAddress: @WriteWith<HttpUrlParceler> HttpUrl,
        val required: Boolean,
    ) : SampleState()
}
