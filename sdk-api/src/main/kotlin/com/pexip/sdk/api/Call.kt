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
package com.pexip.sdk.api

/**
 * Represents an API call.
 */
public interface Call<T> {

    /**
     * Suspends until the [Call] completes with either success or failure.
     *
     * @return successful response body
     */
    public suspend fun await(): T

    /**
     * Executes the call on the caller thread.
     */
    @Deprecated(
        message = "Use suspending await() instead.",
        level = DeprecationLevel.WARNING,
    )
    public fun execute(): T

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * @param callback a callback to invoke on completion
     */
    @Deprecated(
        message = "Use suspending await() instead.",
        level = DeprecationLevel.WARNING,
    )
    public fun enqueue(callback: Callback<T>)

    /**
     * Cancels the request, if possible.
     */
    @Deprecated(
        message = "Use suspending await() instead.",
        level = DeprecationLevel.WARNING,
    )
    public fun cancel()
}
