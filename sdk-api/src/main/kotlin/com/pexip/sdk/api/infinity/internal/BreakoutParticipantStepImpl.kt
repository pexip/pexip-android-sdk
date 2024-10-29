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

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.PreferredAspectRatioRequest
import com.pexip.sdk.api.infinity.RoleRequest
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.Response

internal class BreakoutParticipantStepImpl(
    private val breakoutStep: BreakoutStepImpl,
    override val participantId: ParticipantId,
) : InfinityService.ParticipantStep,
    BreakoutParticipantStepImplScope,
    BreakoutStepImplScope by breakoutStep {

    override fun calls(request: CallsRequest, token: Token): Call<CallsResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .withPathSegment("calls")
            .token(token)
            .build(),
        mapper = { parse(it, CallsResponseSerializer) },
    )

    override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .withPathSegment("dtmf")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun mute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("mute")
            .token(token)
            .build(),
        mapper = ::parseMuteUnmute,
    )

    override fun unmute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("unmute")
            .token(token)
            .build(),
        mapper = ::parseMuteUnmute,
    )

    override fun clientMute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("client_mute")
            .token(token)
            .build(),
        mapper = ::parseClientMuteClientUnmute,
    )

    override fun clientUnmute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("client_unmute")
            .token(token)
            .build(),
        mapper = ::parseClientMuteClientUnmute,
    )

    override fun videoMuted(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("video_muted")
            .token(token)
            .build(),
        mapper = ::parseVideoMutedVideoUnmuted,
    )

    override fun videoUnmuted(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("video_unmuted")
            .token(token)
            .build(),
        mapper = ::parseVideoMutedVideoUnmuted,
    )

    override fun takeFloor(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("take_floor")
            .token(token)
            .build(),
        mapper = ::parseTakeReleaseFloor,
    )

    override fun releaseFloor(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("release_floor")
            .token(token)
            .build(),
        mapper = ::parseTakeReleaseFloor,
    )

    override fun message(request: MessageRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .withPathSegment("message")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun preferredAspectRatio(
        request: PreferredAspectRatioRequest,
        token: Token,
    ): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .withPathSegment("preferred_aspect_ratio")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun buzz(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("buzz")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun clearBuzz(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("clearbuzz")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun spotlightOn(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("spotlighton")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun spotlightOff(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("spotlightoff")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun unlock(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("unlock")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun disconnect(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .withPathSegment("disconnect")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun role(request: RoleRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .withPathSegment("role")
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun call(callId: CallId): InfinityService.CallStep = BreakoutCallStepImpl(this, callId)

    private fun Request.Builder.withPathSegment(pathSegment: String) = url(url) {
        conference(conferenceAlias)
        breakout(breakoutId)
        participant(participantId)
        addPathSegment(pathSegment)
    }

    private fun parseMuteUnmute(response: Response) = when (response.code) {
        200 -> Unit
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseClientMuteClientUnmute(response: Response) = when (response.code) {
        200 -> Unit
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseVideoMutedVideoUnmuted(response: Response) = parseMuteUnmute(response)

    private fun parseTakeReleaseFloor(response: Response) = when (response.code) {
        200 -> Unit
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private inline fun <reified T> parse(
        response: Response,
        deserializer: DeserializationStrategy<T>,
    ) = when (response.code) {
        200 -> json.decodeFromResponseBody(deserializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parse403(): Nothing {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw InvalidTokenException(message)
    }

    private fun Response.parse404(): Nothing = try {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw NoSuchConferenceException(message)
    } catch (e: SerializationException) {
        throw NoSuchNodeException()
    }
}
