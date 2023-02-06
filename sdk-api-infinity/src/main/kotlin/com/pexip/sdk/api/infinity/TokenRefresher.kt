/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            callback = callback,
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
            callback = callback,
        )
    }
}
