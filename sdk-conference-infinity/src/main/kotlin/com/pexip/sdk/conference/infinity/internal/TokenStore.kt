package com.pexip.sdk.conference.infinity.internal

internal interface TokenStore {

    fun get(): String

    fun updateAndGet(block: (String) -> String): String
}
