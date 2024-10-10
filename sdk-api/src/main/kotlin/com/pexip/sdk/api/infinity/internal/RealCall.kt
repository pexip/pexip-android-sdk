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
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class RealCall<T>(
    client: OkHttpClient,
    request: Request,
    private val mapper: (Response) -> T,
) : Call<T> {

    private val call = client.newCall(request)

    override suspend fun await(): T = suspendCancellableCoroutine {
        it.invokeOnCancellation { call.cancel() }
        val callback = object : okhttp3.Callback {

            override fun onResponse(call: okhttp3.Call, response: Response) = try {
                it.resume(response.use(mapper))
            } catch (t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                it.resumeWithException(e)
            }
        }
        call.enqueue(callback)
    }

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun execute(): T = call.execute().use(mapper)

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun enqueue(callback: Callback<T>) {
        val c = object : okhttp3.Callback {

            override fun onResponse(call: okhttp3.Call, response: Response) = try {
                callback.onSuccess(this@RealCall, response.use(mapper))
            } catch (t: Throwable) {
                callback.onFailure(this@RealCall, t)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback.onFailure(this@RealCall, e)
            }
        }
        call.enqueue(c)
    }

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun cancel() = call.cancel()
}
