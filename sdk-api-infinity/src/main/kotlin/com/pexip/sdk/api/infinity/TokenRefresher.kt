package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.RealTokenRefresher
import java.util.concurrent.ScheduledExecutorService

/**
 * A helper class that refreshes the token.
 */
public interface TokenRefresher {

    /**
     * Cancels this token refresher.
     */
    public fun cancel()

    public companion object {

        @JvmStatic
        public fun create(
            step: InfinityService.ConferenceStep,
            store: TokenStore,
            executor: ScheduledExecutorService,
        ): TokenRefresher = RealTokenRefresher(
            store = store,
            refreshToken = step::refreshToken,
            releaseToken = step::releaseToken,
            executor = executor
        )

        @JvmStatic
        public fun create(
            step: InfinityService.RegistrationStep,
            store: TokenStore,
            executor: ScheduledExecutorService,
        ): TokenRefresher = RealTokenRefresher(
            store = store,
            refreshToken = step::refreshToken,
            releaseToken = step::releaseToken,
            executor = executor
        )
    }
}
