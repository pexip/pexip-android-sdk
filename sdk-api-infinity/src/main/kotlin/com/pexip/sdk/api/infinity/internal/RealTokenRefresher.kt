package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenRefresher
import com.pexip.sdk.api.infinity.TokenStore
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class RealTokenRefresher<T : Token>(
    private val store: TokenStore,
    private val refreshToken: (Token) -> Call<T>,
    private val releaseToken: (Token) -> Call<*>,
    private val executor: ScheduledExecutorService,
) : TokenRefresher {

    private val refreshTokenRunnable = Runnable {
        store.updateAndGet { token ->
            try {
                refreshToken(token).execute()
            } catch (t: Throwable) {
                token
            }
        }
    }
    private val releaseTokenRunnable = Runnable {
        try {
            releaseToken(store.get()).execute()
        } catch (t: Throwable) {
            // noop
        }
    }
    private val future = executor.scheduleWithFixedDelay(
        refreshTokenRunnable,
        0,
        store.get().expires / 2,
        TimeUnit.SECONDS,
    )

    override fun cancel() {
        future.cancel(true)
        executor.submit(releaseTokenRunnable)
    }
}
