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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.util.UUID

internal class ParticipantStepImpl(
    override val conferenceStep: ConferenceStepImpl,
    override val participantId: UUID,
) : InfinityService.ParticipantStep,
    ParticipantStepImplScope,
    ConferenceStepImplScope by conferenceStep {

    override fun calls(request: CallsRequest, token: Token): Call<CallsResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("calls")
            }
            .token(token)
            .build(),
        mapper = { parse(it, CallsResponseSerializer) },
    )

    override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("dtmf")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun mute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("mute")
            }
            .token(token)
            .build(),
        mapper = ::parseMuteUnmute,
    )

    override fun unmute(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("unmute")
            }
            .token(token)
            .build(),
        mapper = ::parseMuteUnmute,
    )

    override fun videoMuted(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("video_muted")
            }
            .token(token)
            .build(),
        mapper = ::parseVideoMutedVideoUnmuted,
    )

    override fun videoUnmuted(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("video_unmuted")
            }
            .token(token)
            .build(),
        mapper = ::parseVideoMutedVideoUnmuted,
    )

    override fun takeFloor(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("take_floor")
            }
            .token(token)
            .build(),
        mapper = ::parseTakeReleaseFloor,
    )

    override fun releaseFloor(token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("release_floor")
            }
            .token(token)
            .build(),
        mapper = ::parseTakeReleaseFloor,
    )

    override fun message(request: MessageRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("message")
            }
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
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("preferred_aspect_ratio")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun buzz(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("buzz")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun clearBuzz(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("clearbuzz")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun unlock(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("unlock")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun disconnect(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("disconnect")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun role(request: RoleRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                participant(participantId)
                addPathSegment("role")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun call(callId: UUID): InfinityService.CallStep = CallStepImpl(this, callId)

    private fun parseMuteUnmute(response: Response) = when (response.code) {
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
