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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal interface TestCall<T> : Call<T> {

    override suspend fun await(): T = suspendCancellableCoroutine {
        it.invokeOnCancellation { cancel() }
        val callback = object : Callback<T> {

            override fun onSuccess(call: Call<T>, response: T) = it.resume(response)

            override fun onFailure(call: Call<T>, t: Throwable) = it.resumeWithException(t)
        }
        enqueue(callback)
    }

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun execute(): T = TODO()

    override fun enqueue(callback: Callback<T>): Unit = TODO()

    override fun cancel(): Unit = TODO()
}
