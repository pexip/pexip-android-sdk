package com.pexip.sdk.video.sample.pinrequirement

import okhttp3.HttpUrl

data class PinRequirementProps(
    val nodeAddress: HttpUrl,
    val alias: String,
    val displayName: String,
)
