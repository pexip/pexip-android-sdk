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
package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.registration.FailureRegistrationEvent
import com.pexip.sdk.registration.IncomingCancelledRegistrationEvent
import com.pexip.sdk.registration.IncomingRegistrationEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Suppress("ktlint:standard:function-naming")
internal inline fun RegistrationEvent(
    event: Event,
    at: () -> Instant = Clock.System::now,
) = when (event) {
    is IncomingEvent -> IncomingRegistrationEvent(
        at = at(),
        conferenceAlias = event.conferenceAlias,
        remoteDisplayName = event.remoteDisplayName,
        token = event.token,
    )
    is IncomingCancelledEvent -> IncomingCancelledRegistrationEvent(
        at = at(),
        token = event.token,
    )
    else -> null
}

@Suppress("ktlint:standard:function-naming")
internal inline fun RegistrationEvent(t: Throwable, at: () -> Instant = Clock.System::now) =
    FailureRegistrationEvent(at(), t)
