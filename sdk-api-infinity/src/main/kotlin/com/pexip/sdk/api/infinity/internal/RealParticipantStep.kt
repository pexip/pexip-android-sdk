package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URL
import java.util.UUID

internal class RealParticipantStep(
    private val client: OkHttpClient,
    private val json: Json,
    private val node: URL,
    private val conferenceAlias: String,
    private val participantId: UUID,
) : InfinityService.ParticipantStep {

    override fun calls(request: CallsRequest, token: String): Call<CallsResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            call = client.newCall {
                post(json.encodeToRequestBody(request))
                url(node, conferenceAlias, participantId, "calls")
                header("token", token)
            },
            mapper = ::parseCalls
        )
    }

    override fun call(callId: UUID): InfinityService.CallStep =
        RealCallStep(client, json, node, conferenceAlias, participantId, callId)

    private fun parseCalls(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(CallsResponseSerializer, response.body!!)
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
