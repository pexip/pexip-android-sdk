/*
 * Copyright 2024 Pexip AS
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
package com.pexip.sdk.core

import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * A utility function that retries a given block if it encounters an [IOException].
 *
 * @param times a number of attempts to perform the given block
 * @param initialDelay a start delay between subsequent attempts
 * @param maxDelay a maximum delay between subsequent attempts
 * @param factor a speed at which the delay between subsequent attempts grows
 * @param block a block to perform
 */
@InternalSdkApi
public suspend inline fun <T> retry(
    times: Int = 5,
    initialDelay: Duration = 100.milliseconds,
    maxDelay: Duration = 1.seconds,
    factor: Double = 2.0,
    block: () -> T,
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // noop
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
    }
    return block()
}
