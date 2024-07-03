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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasCause
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.VersionResponse
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.ReferConferenceEvent
import com.pexip.sdk.conference.ReferException
import com.pexip.sdk.conference.Referer
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.ServiceType
import com.pexip.sdk.conference.Theme
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import com.pexip.sdk.media.MediaConnectionSignaling
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class RefererTest {

    private lateinit var event: ReferConferenceEvent
    private lateinit var callTag: String

    @BeforeTest
    fun setUp() {
        event = ReferConferenceEvent(
            at = Clock.System.now(),
            conferenceAlias = Random.nextString(),
            token = Random.nextString(),
        )
        callTag = Random.nextString()
    }

    @Test
    fun `transfer rethrows`() = runTest {
        val t = Throwable()
        val directMedia = Random.nextBoolean()
        val step = object : InfinityService.ConferenceStep {

            override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> {
                assertThat(request::incomingToken).isEqualTo(event.token)
                assertThat(request::directMedia).isEqualTo(directMedia)
                return object : TestCall<RequestTokenResponse> {

                    override fun enqueue(callback: Callback<RequestTokenResponse>) =
                        callback.onFailure(this, t)
                }
            }
        }
        val builder = object : InfinityService.RequestBuilder {

            override fun conference(conferenceAlias: String): InfinityService.ConferenceStep {
                assertThat(conferenceAlias, "conferenceAlias").isEqualTo(event.conferenceAlias)
                return step
            }
        }
        val referer = RefererImpl(builder, callTag, directMedia, ::TestConference)
        assertFailure { referer.refer(event) }
            .isInstanceOf<ReferException>()
            .hasCause(t)
    }

    @Test
    fun `transfer returns a new Conference`() = runTest {
        val response = RequestTokenResponse(
            token = Random.nextString(),
            expires = Random.nextInt(10..120).seconds,
            conferenceName = Random.nextString(),
            participantId = Random.nextParticipantId(),
            participantName = Random.nextString(),
            directMediaRequested = Random.nextBoolean(),
            version = VersionResponse(Random.nextString(), Random.nextString()),
            callTag = callTag,
        )
        val step = object : InfinityService.ConferenceStep {

            override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> {
                assertThat(request::incomingToken).isEqualTo(event.token)
                assertThat(request::directMedia).isEqualTo(response.directMediaRequested)
                assertThat(request::callTag).isEqualTo(response.callTag)
                return object : TestCall<RequestTokenResponse> {

                    override fun enqueue(callback: Callback<RequestTokenResponse>) =
                        callback.onSuccess(this, response)
                }
            }
        }
        val builder = object : InfinityService.RequestBuilder {

            override fun conference(conferenceAlias: String): InfinityService.ConferenceStep {
                assertThat(conferenceAlias, "conferenceAlias").isEqualTo(event.conferenceAlias)
                return step
            }
        }
        val referer = RefererImpl(builder, callTag, response.directMediaRequested, ::TestConference)
        assertThat(referer.refer(event), "conference").isEqualTo(TestConference(step, response))
    }

    private data class TestConference(
        val step: InfinityService.ConferenceStep,
        val response: RequestTokenResponse,
    ) : Conference {

        override val name: String get() = TODO()
        override val theme: Theme get() = TODO()
        override val roster: Roster get() = TODO()
        override val referer: Referer get() = TODO()
        override val messenger: Messenger get() = TODO()
        override val serviceType: ServiceType get() = TODO()
        override val signaling: MediaConnectionSignaling get() = TODO()

        override fun registerConferenceEventListener(listener: ConferenceEventListener): Unit =
            TODO()

        override fun unregisterConferenceEventListener(listener: ConferenceEventListener): Unit =
            TODO()

        override fun leave(): Unit = TODO()
    }
}
