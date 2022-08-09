package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenRefresher
import com.pexip.sdk.api.infinity.TokenStore
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class RealTokenRefresher<T : Token>(
    private val store: TokenStore,
    private val refreshToken: (Token) -> Call<T>,
    private val releaseToken: (Token) -> Call<*>,
    private val executor: ScheduledExecutorService,
) : TokenRefresher {

    private val refreshTokenRunnable = Runnable {
        runCatching(store::get)
            .mapCatching(refreshToken)
            .mapCatching { it.execute() }
            .onSuccess(store::set)
            .onSuccess(::scheduleRefresh)
    }
    private val releaseTokenRunnable = Runnable {
        runCatching(store::get)
            .mapCatching(releaseToken)
            .mapCatching { it.execute() }
    }

    @Volatile
    private var future: Future<*> = executor.submit(refreshTokenRunnable)

    override fun cancel() {
        future.cancel(true)
        executor.submit(releaseTokenRunnable)
    }

    private fun scheduleRefresh(token: Token) {
        if (executor.isShutdown) return
        future = executor.schedule(refreshTokenRunnable, token.expires / 2, TimeUnit.SECONDS)
    }
}
