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
    private val callback: TokenRefresher.Callback,
) : TokenRefresher {

    private val refreshTokenRunnable = Runnable {
        runCatching(store::get)
            .mapCatching(refreshToken)
            .mapCatching { it.execute() }
            .onSuccess(store::set)
            .onSuccess(::scheduleRefresh)
            .onFailure(callback::onFailure)
    }
    private val releaseTokenRunnable = Runnable {
        runCatching(store::get)
            .mapCatching(releaseToken)
            .mapCatching { it.execute() }
    }

    @Volatile
    private var future: Future<*> =
        executor.schedule(refreshTokenRunnable, getDelay(), TimeUnit.SECONDS)

    override fun cancel() {
        future.cancel(true)
        executor.submit(releaseTokenRunnable)
    }

    private fun scheduleRefresh(token: Token) {
        if (executor.isShutdown) return
        future = executor.schedule(refreshTokenRunnable, getDelay(token), TimeUnit.SECONDS)
    }

    private fun getDelay(token: Token = store.get()) = token.expires / 2
}
