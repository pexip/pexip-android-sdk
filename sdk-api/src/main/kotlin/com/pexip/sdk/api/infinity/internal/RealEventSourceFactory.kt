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

import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.EventSourceListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

internal class RealEventSourceFactory(client: OkHttpClient, private val request: Request) :
    EventSourceFactory {

    private val factory = EventSources.createFactory(client) { readTimeout(0, TimeUnit.SECONDS) }

    override fun create(listener: EventSourceListener): EventSource =
        RealEventSource(factory, request, listener)
}
