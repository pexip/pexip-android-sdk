package com.pexip.sdk.video.internal

import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

internal class TokenStore(token: String, val expires: Duration) {

    private val _token = AtomicReference(token)

    var token: String
        get() = _token.get()
        set(value) = _token.set(value)
}
