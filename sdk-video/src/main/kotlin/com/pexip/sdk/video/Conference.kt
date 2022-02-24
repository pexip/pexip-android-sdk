package com.pexip.sdk.video

import com.pexip.sdk.video.internal.InfinityService
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.RealInfinityService
import com.pexip.sdk.video.internal.TokenHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import okhttp3.OkHttpClient

public class Conference private constructor(client: OkHttpClient, token: Token) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val service: InfinityService = RealInfinityService(
        client = client,
        node = token.node,
        joinDetails = token.joinDetails,
        token = token.value
    )
    private val tokenHandler = TokenHandler(service)
    private val events = service.events().shareIn(scope, SharingStarted.Eagerly)

    init {
        tokenHandler.launchIn(scope)
    }

    public fun leave() {
        scope.cancel()
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
