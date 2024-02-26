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

import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.conference.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.random.Random

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextDigits(length: Int) =
    CharArray(length) { DtmfRequest.ALLOWED_DIGITS.random(this) }.concatToString()

internal fun Random.nextToken() = RefreshTokenResponse(
    token = nextString(8),
    expires = (60L..600L).random(this),
)

internal fun Random.nextMessage(at: Long = System.currentTimeMillis(), direct: Boolean = false) =
    Message(
        at = at,
        participantId = UUID.randomUUID(),
        participantName = nextString(8),
        type = nextString(8),
        payload = nextString(64),
        direct = direct,
    )

internal suspend fun <T> MutableSharedFlow<T>.awaitSubscriptionCountAtLeast(threshold: Int): Int {
    require(threshold > 0) { "threshold must be a positive number." }
    return subscriptionCount.first { it >= threshold }
}
