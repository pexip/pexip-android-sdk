package com.pexip.sdk.conference.infinity.internal

import java.util.concurrent.atomic.AtomicReference

internal class RealTokenStore(token: String) : TokenStore {

    private val token = AtomicReference(token)

    override fun get(): String = token.get()

    override fun updateAndGet(block: (String) -> String): String = token.updateAndGet(block)
}
