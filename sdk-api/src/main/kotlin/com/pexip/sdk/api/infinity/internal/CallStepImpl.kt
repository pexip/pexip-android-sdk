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
import com.pexip.sdk.api.infinity.AckRequest
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse
import com.pexip.sdk.infinity.CallId
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal class CallStepImpl(
    override val participantStep: ParticipantStepImpl,
    private val callId: CallId,
) : InfinityService.CallStep, ParticipantStepImplScope by participantStep {

    override fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(url) {
                conference(conferenceAlias)
                participant(participantId)
                call(callId)
                addPathSegment("new_candidate")
            }
            .token(token)
            .build(),
        mapper = ::parseNewCandidate,
    )

    override fun ack(token: Token): Call<Unit> = RealCall(
        client = client.newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .build(),
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(url) {
                conference(conferenceAlias)
                participant(participantId)
                call(callId)
                addPathSegment("ack")
            }
            .token(token)
            .build(),
        mapper = ::parseAck,
    )

    override fun ack(request: AckRequest, token: Token): Call<Unit> = RealCall(
        client = client.newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .build(),
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(url) {
                conference(conferenceAlias)
                participant(participantId)
                call(callId)
                addPathSegment("ack")
            }
            .token(token)
            .build(),
        mapper = ::parseAck,
    )

    override fun update(request: UpdateRequest, token: Token): Call<UpdateResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(url) {
                conference(conferenceAlias)
                participant(participantId)
                call(callId)
                addPathSegment("update")
            }
            .token(token)
            .build(),
        mapper = ::parseUpdate,
    )

    override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(url) {
                conference(conferenceAlias)
                participant(participantId)
                call(callId)
                addPathSegment("dtmf")
            }
            .token(token)
            .build(),
        mapper = ::parseDtmf,
    )

    private fun parseNewCandidate(response: Response) = when (response.code) {
        200 -> Unit
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseAck(response: Response) = when (response.code) {
        200 -> Unit
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseUpdate(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(UpdateResponseSerializer, response.body!!)
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
