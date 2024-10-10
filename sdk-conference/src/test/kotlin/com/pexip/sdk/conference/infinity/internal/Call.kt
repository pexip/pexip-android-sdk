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

internal fun <T> call(block: suspend () -> T): Call<T> = object : Call<T> {

    override suspend fun await(): T = block()

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun execute(): T = throw NotImplementedError()

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun enqueue(callback: Callback<T>) = throw NotImplementedError()

    @Deprecated("Use suspending await() instead.", level = DeprecationLevel.WARNING)
    override fun cancel() = throw NotImplementedError()
}
