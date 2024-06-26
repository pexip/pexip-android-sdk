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
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.seconds

internal fun Random.nextDigits(length: Int = 8) =
    CharArray(length) { DtmfRequest.ALLOWED_DIGITS.random(this) }.concatToString()

internal fun Random.nextToken() = RefreshTokenResponse(
    token = nextString(),
    expires = nextInt(10..120).seconds,
)

internal fun Random.nextMessage(at: Instant = Clock.System.now(), direct: Boolean = false) =
    Message(
        at = at,
        participantId = nextParticipantId(),
        participantName = nextString(),
        type = nextString(),
        payload = nextString(64),
        direct = direct,
    )
