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
package com.pexip.sdk.infinity.test

import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.RegistrationId
import com.pexip.sdk.infinity.VersionId
import kotlin.random.Random

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

/**
 * Gets the next random [String] from the random number generator.
 *
 * @param length a length of the [String]
 */
public fun Random.nextString(length: Int = 10): String = buildString(length) {
    repeat(length) { append(CHARACTERS.random(this@nextString)) }
}

/**
 * Gets the next random [CallId] from the random number generator.
 */
public fun Random.nextCallId(): CallId = CallId(nextString())

/**
 * Gets the next random [LayoutId] from the random number generator.
 */
public fun Random.nextLayoutId(): LayoutId = LayoutId(nextString())

/**
 * Gets the next random [ParticipantId] from the random number generator.
 */
public fun Random.nextParticipantId(): ParticipantId = ParticipantId(nextString())

/**
 * Gets the next random [BreakoutId] from the random number generator.
 */
public fun Random.nextBreakoutId(): BreakoutId = BreakoutId(nextString())

/**
 * Gets the next random [RegistrationId] from the random number generator.
 */
public fun Random.nextRegistrationId(): RegistrationId = RegistrationId(nextString())

/**
 * Gets the next random [VersionId] from the random number generator.
 */
public fun Random.nextVersionId(): VersionId = VersionId(nextString())
