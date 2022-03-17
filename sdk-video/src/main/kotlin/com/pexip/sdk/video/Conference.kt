package com.pexip.sdk.video

import com.pexip.sdk.video.internal.InfinityService
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.RealInfinityService
import com.pexip.sdk.video.internal.TokenHandler
import com.pexip.sdk.video.internal.TokenStore
import okhttp3.OkHttpClient

public class Conference private constructor(client: OkHttpClient, token: Token) {

    private val store = TokenStore(token.token, token.expires)
    private val service: InfinityService = RealInfinityService(
        client = client,
        store = store,
        node = token.node,
        joinDetails = token.joinDetails,
        participantId = token.participantId,
    )
    private val tokenHandler = TokenHandler(store, service)

    public val callHandler: CallHandler = CallHandler(service)

    public fun leave() {
        callHandler.dispose()
        tokenHandler.dispose()
    }

    public class Builder {

        private var token: Token? = null
        private var client: OkHttpClient? = null

        public fun token(token: Token): Builder = apply {
            this.token = token
        }

        public fun client(client: OkHttpClient): Builder = apply {
            this.client = client
        }

        public fun build(): Conference = Conference(
            token = checkNotNull(token) { "token is not set." },
            client = client ?: OkHttpClient
        )
    }
}
