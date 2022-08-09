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

    public fun interface Callback {

        /**
         * Invoked when an exception occurred during refresh.
         */
        public fun onFailure(t: Throwable)
    }

    public companion object {

        @JvmStatic
        public fun create(
            step: InfinityService.ConferenceStep,
            store: TokenStore,
            executor: ScheduledExecutorService,
            callback: Callback,
        ): TokenRefresher = RealTokenRefresher(
            store = store,
            refreshToken = step::refreshToken,
            releaseToken = step::releaseToken,
            executor = executor,
            callback = callback
        )

        @JvmStatic
        public fun create(
            step: InfinityService.RegistrationStep,
            store: TokenStore,
            executor: ScheduledExecutorService,
            callback: Callback,
        ): TokenRefresher = RealTokenRefresher(
            store = store,
            refreshToken = step::refreshToken,
            releaseToken = step::releaseToken,
            executor = executor,
            callback = callback
        )
    }
}
