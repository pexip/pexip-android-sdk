package com.pexip.sdk.video.conference.internal

internal interface TokenStore {

    fun get(): String

    fun updateAndGet(block: (String) -> String): String
}
