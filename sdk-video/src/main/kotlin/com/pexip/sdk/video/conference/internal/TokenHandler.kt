package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Node
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class TokenHandler(
    private val node: Node,
    private val conferenceAlias: ConferenceAlias,
    private val store: TokenStore,
    private val service: InfinityService,
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
) {

    private val refreshTokenRunnable = Runnable {
        val response = service.newRequest(node)
            .conference(conferenceAlias)
            .refreshToken(store.token)
            .execute()
        store.token = response.token
    }
    private val releaseTokenRunnable = Runnable {
        service.newRequest(node)
            .conference(conferenceAlias)
            .releaseToken(store.token)
            .execute()
    }
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
