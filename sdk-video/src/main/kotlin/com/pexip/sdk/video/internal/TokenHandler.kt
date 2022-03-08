package com.pexip.sdk.video.internal

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class TokenHandler(
    private val store: TokenStore,
    private val service: InfinityService,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
) {

    private val refreshTokenRunnable = Runnable { store.token = service.refreshToken() }
    private val releaseTokenRunnable = Runnable { service.releaseToken() }
    private val refreshTokenFuture = executor.scheduleWithFixedDelay(
        refreshTokenRunnable,
        0,
        store.expires.inWholeMilliseconds / 2,
        TimeUnit.MILLISECONDS,
    )

    fun dispose() {
        Logger.log("TokenHandler.dispose()")
        refreshTokenFuture.cancel(true)
        executor.submit(releaseTokenRunnable)
        executor.shutdown()
    }
}
