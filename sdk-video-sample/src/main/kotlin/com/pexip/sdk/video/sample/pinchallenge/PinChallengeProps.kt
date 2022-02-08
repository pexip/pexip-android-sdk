package com.pexip.sdk.video.sample.pinchallenge

import okhttp3.HttpUrl

data class PinChallengeProps(
    val nodeAddress: HttpUrl,
    val alias: String,
    val displayName: String,
    val required: Boolean,
)
