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
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package com.pexip.sdk.api.coroutines

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSourceFactory
import kotlinx.coroutines.flow.Flow

/**
 * Suspends until the [Call] completes with either success or failure.
 *
 * @param T successful response body type
 * @return successful response body
 */
@Deprecated("Moved to Call", level = DeprecationLevel.ERROR)
public suspend fun <T> Call<T>.await(): T = await()

/**
 * Converts this [EventSourceFactory] to a [Flow].
 *
 * @return a [Flow] of [Event]s
 */
@Deprecated("Moved to EventSourceFactory", level = DeprecationLevel.ERROR)
public fun EventSourceFactory.asFlow(): Flow<Event> = asFlow()
