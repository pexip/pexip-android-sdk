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
import com.pexip.sdk.api.Callback
import okhttp3.Response
import java.io.IOException

internal class RealCallback<T>(
    private val call: Call<T>,
    private val callback: Callback<T>,
    private val mapper: (Response) -> T,
) : okhttp3.Callback {

    override fun onResponse(call: okhttp3.Call, response: Response) = try {
        callback.onSuccess(this.call, response.use(mapper))
    } catch (t: Throwable) {
        callback.onFailure(this.call, t)
    }

    override fun onFailure(call: okhttp3.Call, e: IOException) {
        callback.onFailure(this.call, e)
    }
}
