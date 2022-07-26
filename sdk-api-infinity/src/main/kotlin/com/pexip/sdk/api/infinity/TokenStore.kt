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
     * Updates and gets the token.
     *
     * @param block a block that should return an updated token
     * @return an updated token
     */
    public fun updateAndGet(block: (Token) -> Token): Token

    public companion object {

        @JvmStatic
        public fun create(token: Token): TokenStore = RealTokenStore(token)
    }
}
