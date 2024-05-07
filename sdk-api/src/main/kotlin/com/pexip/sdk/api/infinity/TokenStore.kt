/*
 * Copyright 2022-2024 Pexip AS
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

import com.pexip.sdk.core.InternalSdkApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A token store.
 */
@InternalSdkApi
public class TokenStore(token: Token) {

    private val _token = MutableStateFlow(token)

    /**
     * The store token.
     */
    public val token: StateFlow<Token> = _token.asStateFlow()

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
    public fun refreshTokenIn(
        scope: CoroutineScope,
        refreshToken: suspend (Token) -> Token,
        releaseToken: suspend (Token) -> Unit,
        onFailure: suspend (t: Throwable) -> Unit,
    ): Job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        try {
            while (isActive) {
                val newToken = update(refreshToken)
                delay(newToken.expires / 2)
            }
        } catch (e: CancellationException) {
            ensureActive()
        } catch (t: Throwable) {
            onFailure(t)
        } finally {
            withContext(NonCancellable) {
                try {
                    releaseToken(token.value)
                } catch (t: Throwable) {
                    // noop
                }
            }
        }
    }

    internal suspend fun update(block: suspend (Token) -> Token): Token {
        val token = block(_token.value)
        _token.value = token
        return token
    }

    public companion object {

        @JvmStatic
        @Deprecated(
            message = "Should not be used by the consumers of the SDK.",
            replaceWith = ReplaceWith(expression = "TokenStore(token)"),
            level = DeprecationLevel.ERROR,
        )
        public fun create(token: Token): TokenStore = TokenStore(token)

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
        @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
        @Deprecated(
            message = "Replaced by an instance method.",
            level = DeprecationLevel.ERROR,
        )
        public fun TokenStore.refreshTokenIn(
            scope: CoroutineScope,
            refreshToken: suspend (Token) -> Token,
            releaseToken: suspend (Token) -> Unit,
            onFailure: suspend (t: Throwable) -> Unit,
        ): Job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                while (isActive) {
                    val newToken = update(refreshToken)
                    delay(newToken.expires / 2)
                }
            } catch (e: CancellationException) {
                ensureActive()
            } catch (t: Throwable) {
                onFailure(t)
            } finally {
                withContext(NonCancellable) {
                    try {
                        releaseToken(token.value)
                    } catch (t: Throwable) {
                        // noop
                    }
                }
            }
        }
    }
}
