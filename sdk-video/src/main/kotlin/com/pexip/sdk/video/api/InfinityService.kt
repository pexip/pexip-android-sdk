@file:JvmName("InfinityServiceFactory")

package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException

/**
 * Pexip client REST API v2.
 */
public interface InfinityService {

    /**
     * Requests a new token from the conferencing node.
     *
     * @param nodeAddress a node address in the form of https://example.com
     * @param alias a conference alias
     * @param displayName a name for this participant
     * @param pin an optional PIN
     * @return a [Token] for this conference
     * @throws RequiredPinException if either host or guest PIN is required (if [pin] was null)
     * @throws InvalidPinException if the supplied [pin] is invalid
     * @throws NoSuchNodeException if supplied [nodeAddress] doesn't have a deployment
     * @throws NoSuchConferenceException if supplied [alias] did not match any aliases or
     * call routing rules
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun requestToken(
        nodeAddress: HttpUrl,
        alias: String,
        displayName: String,
        pin: String?,
    ): Token

    /**
     * Refreshes a token to get a new one.
     *
     * @param nodeAddress a node address in the form of https://example.com
     * @param alias a conference alias
     * @param a token to refresh
     * @return a [Token] for this conference
     * @throws InvalidTokenException if a token is invalid or has already expired
     * @throws NoSuchNodeException if supplied [nodeAddress] doesn't have a deployment
     * @throws NoSuchConferenceException if supplied [alias] did not match any aliases or
     * call routing rules
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun refreshToken(nodeAddress: HttpUrl, alias: String, token: String): Token

    /**
     * Releases the token (effectively a disconnect for the participant).
     *
     * Does nothing if the token has already expired.
     *
     * @param nodeAddress a node address in the form of https://example.com
     * @param alias a conference alias
     * @param token a token to release
     * @throws NoSuchNodeException if supplied [nodeAddress] doesn't have a deployment
     * @throws NoSuchConferenceException if supplied [alias] did not match any aliases or
     * call routing rules
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun releaseToken(nodeAddress: HttpUrl, alias: String, token: String)
}

@JvmName("create")
public fun InfinityService(client: OkHttpClient): InfinityService = OkHttpInfinityService(client)
