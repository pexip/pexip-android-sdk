/*
 * Copyright 2023-2024 Pexip AS
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

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ReferConferenceEvent
import com.pexip.sdk.conference.ReferException
import com.pexip.sdk.conference.Referer
import com.pexip.sdk.conference.infinity.InfinityConference
import com.pexip.sdk.core.retry
import kotlinx.coroutines.CancellationException

internal class RefererImpl(
    private val step: InfinityService.ConferenceStep,
    private val callTag: String,
    private val directMedia: Boolean,
    private val createConference: (InfinityService.ConferenceStep, RequestTokenResponse) -> Conference,
) : Referer {

    constructor(
        step: InfinityService.ConferenceStep,
        callTag: String,
        directMedia: Boolean,
    ) : this(
        step = step,
        callTag = callTag,
        directMedia = directMedia,
        createConference = InfinityConference::create,
    )

    override suspend fun refer(event: ReferConferenceEvent): Conference = try {
        val step = step.conference(event.conferenceAlias)
        val request = RequestTokenRequest(
            incomingToken = event.token,
            directMedia = directMedia,
            callTag = callTag,
        )
        val response = retry { step.requestToken(request).await() }
        createConference(step, response)
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        throw ReferException(t)
    }
}
