/*
 * Copyright 2022-2023 Pexip AS
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
import com.pexip.sdk.api.infinity.Token
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

    override fun calls(request: CallsRequest, token: String): Call<CallsResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("calls")
                }
                .header("token", token)
                .build(),
            mapper = ::parseCalls,
        )
    }

    override fun calls(request: CallsRequest, token: Token): Call<CallsResponse> =
        calls(request, token.token)

    override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("dtmf")
                }
                .header("token", token)
                .build(),
            mapper = ::parseDtmf,
        )
    }

    override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
        dtmf(request, token.token)

    override fun mute(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("mute")
                }
                .header("token", token)
                .build(),
            mapper = ::parseMuteUnmute,
        )
    }

    override fun mute(token: Token): Call<Unit> = mute(token.token)

    override fun unmute(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("unmute")
                }
                .header("token", token)
                .build(),
            mapper = ::parseMuteUnmute,
        )
    }

    override fun unmute(token: Token): Call<Unit> = unmute(token.token)

    override fun videoMuted(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("video_muted")
                }
                .header("token", token)
                .build(),
            mapper = ::parseVideoMutedVideoUnmuted,
        )
    }

    override fun videoMuted(token: Token): Call<Unit> = videoMuted(token.token)

    override fun videoUnmuted(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("video_unmuted")
                }
                .header("token", token)
                .build(),
            mapper = ::parseVideoMutedVideoUnmuted,
        )
    }

    override fun videoUnmuted(token: Token): Call<Unit> = videoUnmuted(token.token)

    override fun takeFloor(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("take_floor")
                }
                .header("token", token)
                .build(),
            mapper = ::parseTakeReleaseFloor,
        )
    }

    override fun takeFloor(token: Token): Call<Unit> = takeFloor(token.token)

    override fun releaseFloor(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("release_floor")
                }
                .header("token", token)
                .build(),
            mapper = ::parseTakeReleaseFloor,
        )
    }

    override fun releaseFloor(token: Token): Call<Unit> = releaseFloor(token.token)

    override fun message(request: MessageRequest, token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("message")
                }
                .header("token", token)
                .build(),
            mapper = ::parseMessage,
        )
    }

    override fun message(request: MessageRequest, token: Token): Call<Boolean> =
        message(request, token.token)

    override fun preferredAspectRatio(
        request: PreferredAspectRatioRequest,
        token: String,
    ): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(node) {
                    conference(conferenceAlias)
                    participant(participantId)
                    addPathSegment("preferred_aspect_ratio")
                }
                .header("token", token)
                .build(),
            mapper = ::parsePreferredAspectRatio,
        )
    }

    override fun preferredAspectRatio(
        request: PreferredAspectRatioRequest,
        token: Token,
    ): Call<Boolean> =
        preferredAspectRatio(request, token.token)

    override fun call(callId: UUID): InfinityService.CallStep = CallStepImpl(this, callId)

    private fun parseCalls(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(CallsResponseSerializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseDtmf(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

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

    private fun parseMessage(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parsePreferredAspectRatio(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
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
