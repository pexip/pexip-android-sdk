package com.pexip.sdk.video.internal

import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Node
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.internal.EMPTY_REQUEST
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

internal class RealInfinityService(
    private val client: OkHttpClient,
    private val node: Node,
    private val joinDetails: JoinDetails,
    token: String,
) : InfinityService {

    private val sseClient = client.newBuilder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    private val factory = EventSources.createFactory(sseClient)
    private val token = MutableStateFlow(token)

    override fun events(): Flow<Event> = factory
        .events(
            request = {
                Request {
                    get()
                    url(node.address.resolve("api/client/v2/conferences/${joinDetails.alias}/events")!!)
                    header("token", token.value)
                }
            },
            handler = Event::from
        )
        .retryWhen { _, attempt ->
            delay(attempt.coerceAtMost(3) * 1000)
            true
        }

    override suspend fun refreshToken(): Duration {
        val response = client.await {
            post(EMPTY_REQUEST)
            url(node.address.resolve("api/client/v2/conferences/${joinDetails.alias}/refresh_token")!!)
            header("token", token.value)
        }
        val r = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody(RequestToken200Serializer, it.body!!)
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
        token.value = r.token
        return r.expires
    }

    override suspend fun releaseToken() = try {
        val response = client.await {
            post(EMPTY_REQUEST)
            url(node.address.resolve("api/client/v2/conferences/${joinDetails.alias}/release_token")!!)
            header("token", token.value)
        }
        response.close()
    } catch (e: Exception) {
        // noop
    }
}
