package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class RealTokenRefresher(
    expires: Long,
    private val store: TokenStore,
    private val conferenceStep: InfinityService.ConferenceStep,
    private val executor: ScheduledExecutorService,
) : TokenRefresher {

    private val refreshTokenRunnable = Runnable {
        store.updateAndGet { token ->
            try {
                val response = conferenceStep
                    .refreshToken(token)
                    .execute()
                response.token
            } catch (t: Throwable) {
                token
            }
        }
    }
    private val releaseTokenRunnable = Runnable {
        try {
            conferenceStep
                .releaseToken(store.get())
                .execute()
        } catch (t: Throwable) {
            // noop
        }
    }
    private val future = executor.scheduleWithFixedDelay(
        refreshTokenRunnable,
        0,
        expires / 2,
        TimeUnit.SECONDS,
    )

    override fun cancel() {
        future.cancel(true)
        executor.submit(releaseTokenRunnable)
    }
}
