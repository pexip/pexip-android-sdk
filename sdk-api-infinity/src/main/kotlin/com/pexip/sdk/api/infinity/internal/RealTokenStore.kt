package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import java.util.concurrent.atomic.AtomicReference

internal class RealTokenStore(token: Token) : TokenStore {

    private val token = AtomicReference(token)

    override fun get(): Token = token.get()

    override fun updateAndGet(block: (Token) -> Token): Token = with(token) {
        var prev: Token
        var next: Token
        do {
            prev = get()
            next = block(prev)
        } while (!compareAndSet(prev, next))
        return next
    }
}
