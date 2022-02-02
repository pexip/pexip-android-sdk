package com.pexip.sdk.video.sample.pinchallenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PinChallengeState(
    val pin: String = "",
    val t: Throwable? = null,
    val requesting: Boolean = false,
    val pinToSubmit: String? = null,
) : Parcelable
