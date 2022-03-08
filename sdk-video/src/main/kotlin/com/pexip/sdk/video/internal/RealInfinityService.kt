package com.pexip.sdk.video.internal

import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Node
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.internal.EMPTY_REQUEST
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

internal class RealInfinityService(
    private val client: OkHttpClient,
    private val store: TokenStore,
    private val node: Node,
    private val joinDetails: JoinDetails,
    private val participantId: String,
) : InfinityService {

    private val sseClient = client.newBuilder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    private val factory = EventSources.createFactory(sseClient)

    override val events: Flow<Event> = factory
        .events(
            request = {
                Request {
                    get()
                    url(node.address) {
                        addPathSegments("api/client/v2/conferences")
                        addPathSegment(joinDetails.alias)
                        addPathSegment("events")
                    }
                    header("token", store.token)
                }
            },
            handler = Event::from
        )
        .retryWhen { _, attempt ->
            delay(attempt.coerceAtMost(3) * 1000)
            true
        }

    override fun refreshToken(): String {
        val response = client.execute {
            post(EMPTY_REQUEST)
            url(node.address) {
                addPathSegments("api/client/v2/conferences")
                addPathSegment(joinDetails.alias)
                addPathSegment("refresh_token")
            }
            header("token", store.token)
        }
        val r = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody(RefreshToken200ResponseSerializer, it.body!!)
                403 -> {
                    val message = Json.decodeFromResponseBody(StringSerializer, it.body!!)
                    throw InvalidTokenException(message)
                }
                404 -> try {
                    val message = Json.decodeFromResponseBody(StringSerializer, it.body!!)
                    throw NoSuchConferenceException(message)
                } catch (e: SerializationException) {
                    throw NoSuchNodeException()
                }
                else -> throw IllegalStateException()
            }
        }
        return r.token
    }

    override fun releaseToken() = try {
        val response = client.execute {
            post(EMPTY_REQUEST)
            url(node.address) {
                addPathSegments("api/client/v2/conferences")
                addPathSegment(joinDetails.alias)
                addPathSegment("release_token")
            }
            header("token", store.token)
        }
        response.close()
    } catch (e: Exception) {
        // noop
    }

    override fun calls(request: CallsRequest): CallsResponse {
        val response = client.execute {
            post(Json.encodeToRequestBody(request))
            url(node.address) {
                addPathSegments("api/client/v2/conferences")
                addPathSegment(joinDetails.alias)
                addPathSegment("participants")
                addPathSegment(participantId)
                addPathSegment("calls")
            }
            header("token", store.token)
        }
        return response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody(CallsResponseSerializer, it.body!!)
                else -> throw IllegalStateException()
            }
        }
    }

    override fun ack(request: AckRequest) {
        val response = client.execute {
            post(EMPTY_REQUEST)
            url(node.address) {
                addPathSegments("api/client/v2/conferences")
                addPathSegment(joinDetails.alias)
                addPathSegment("participants")
                addPathSegment(participantId)
                addPathSegment("calls")
                addPathSegment(request.callId)
                addPathSegment("ack")
            }
            header("token", store.token)
        }
        response.close()
    }

    override fun newCandidate(request: CandidateRequest) {
        val response = client.execute {
            post(Json.encodeToRequestBody(request))
            url(node.address) {
                addPathSegments("api/client/v2/conferences")
                addPathSegment(joinDetails.alias)
                addPathSegment("participants")
                addPathSegment(participantId)
                addPathSegment("calls")
                addPathSegment(request.callId)
                addPathSegment("new_candidate")
            }
            header("token", store.token)
        }
        response.close()
    }
}
