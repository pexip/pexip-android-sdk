package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.util.UUID

internal class RealParticipantStep(
    private val client: OkHttpClient,
    private val json: Json,
    private val url: HttpUrl,
) : InfinityService.ParticipantStep {

    override fun calls(request: CallsRequest, token: String): Call<CallsResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(HttpUrl(url) { addPathSegment("calls") })
                .header("token", token)
                .build(),
            mapper = ::parseCalls
        )
    }

    override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(HttpUrl(url) { addPathSegment("dtmf") })
                .header("token", token)
                .build(),
            mapper = ::parseDtmf
        )
    }

    override fun mute(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("mute") })
                .header("token", token)
                .build(),
            mapper = ::parseMuteUnmute
        )
    }

    override fun unmute(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("unmute") })
                .header("token", token)
                .build(),
            mapper = ::parseMuteUnmute
        )
    }

    override fun videoMuted(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("video_muted") })
                .header("token", token)
                .build(),
            mapper = ::parseVideoMutedVideoUnmuted
        )
    }

    override fun videoUnmuted(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("video_unmuted") })
                .header("token", token)
                .build(),
            mapper = ::parseVideoMutedVideoUnmuted
        )
    }

    override fun takeFloor(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("take_floor") })
                .header("token", token)
                .build(),
            mapper = ::parseTakeReleaseFloor
        )
    }

    override fun releaseFloor(token: String): Call<Unit> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("release_floor") })
                .header("token", token)
                .build(),
            mapper = ::parseTakeReleaseFloor
        )
    }

    override fun call(callId: UUID): InfinityService.CallStep =
        RealCallStep(
            client = client,
            json = json,
            url = HttpUrl(url) {
                addPathSegment("calls")
                addPathSegment(callId.toString())
            }
        )

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
