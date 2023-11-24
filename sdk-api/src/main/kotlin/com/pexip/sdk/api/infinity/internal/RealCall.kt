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
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal class RealCall<T>(
    client: OkHttpClient,
    request: Request,
    private val mapper: (Response) -> T,
) : Call<T> {

    private val call = client.newCall(request)

    override fun execute(): T = call.execute().use(mapper)

    override fun enqueue(callback: Callback<T>) =
        call.enqueue(RealCallback(this, callback, mapper))

    override fun cancel() = call.cancel()
}
