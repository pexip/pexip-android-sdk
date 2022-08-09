package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import java.util.concurrent.atomic.AtomicReference

internal class RealTokenStore(token: Token) : TokenStore {

    private val _token = AtomicReference(token)

    override fun get(): Token = _token.get()

    override fun set(token: Token): Unit = _token.set(token)
}
