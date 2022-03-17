package com.pexip.sdk.video.internal

import kotlin.time.Duration

internal class TokenStore(
    @Volatile
    var token: String,
    val expires: Duration,
)
