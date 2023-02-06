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
package com.pexip.sdk.api

/**
 * Represents an API call.
 */
public interface Call<T> {

    /**
     * Executes the call on the caller thread.
     */
    public fun execute(): T

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * @param callback a callback to invoke on completion
     */
    public fun enqueue(callback: Callback<T>)

    /**
     * Cancels the request, if possible.
     */
    public fun cancel()
}
