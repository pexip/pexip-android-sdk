/*
 * Copyright 2022-2023 Pexip AS
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

import com.pexip.sdk.api.infinity.internal.RealTokenStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * A token store.
 */
public interface TokenStore {

    /**
     * Gets the token.
     *
     * @return a token
     */
    public fun get(): Token

    /**
     * Sets the token.
     *
     * @param token a token
     */
    public fun set(token: Token)

    public companion object {

        @JvmStatic
        public fun create(token: Token): TokenStore = RealTokenStore(token)

        /**
         * A helper function to refresh and release the [Token] found in [TokenStore].
         *
         * Note that this function is not intended for consumers to use directly.
         *
         * @param scope a [CoroutineScope] to launch the refresh process in
         * @param refreshToken a function that refreshes the [Token]
         * @param releaseToken a function that releases the [Token]
         * @param onFailure a callback to notify callers about failures
         */
        public inline fun TokenStore.refreshTokenIn(
            scope: CoroutineScope,
            crossinline refreshToken: suspend (Token) -> Token,
            crossinline releaseToken: suspend (Token) -> Unit,
            crossinline onFailure: suspend (t: Throwable) -> Unit,
        ): Job = scope.launch {
            try {
                while (isActive) {
                    val response = refreshToken(get())
                    set(response)
                    delay(response.expires.seconds / 2)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                onFailure(t)
            } finally {
                withContext(NonCancellable) {
                    releaseToken(get())
                }
            }
        }
    }
}
