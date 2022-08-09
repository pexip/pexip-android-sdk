package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.RealTokenStore

/**
 * A token store.
 */
public interface TokenStore {

    /**
     * Gets the token.
     *
     * @return a token
     */
    public fun get(): Token

    /**
     * Sets the token.
     *
     * @param token a token
     */
    public fun set(token: Token)

    public companion object {

        @JvmStatic
        public fun create(token: Token): TokenStore = RealTokenStore(token)
    }
}
