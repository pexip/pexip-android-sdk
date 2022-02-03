@file:JvmName("InfinityServiceFactory")

package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import okhttp3.OkHttpClient
import java.io.IOException

/**
 * Pexip client REST API v2.
 */
interface InfinityService {

    /**
     * Checks whether a conferencing node is in maintenance mode.
     *
     * @param nodeAddress a node address in the form of https://example.com
     * @return true if the node is in maintenance mode, false otherwise
     * @throws NoSuchNodeException if supplied [nodeAddress] doesn't have a deployment
     * @throws IOException if a network error was encountered during operation
     */
    suspend fun isInMaintenanceMode(nodeAddress: String): Boolean

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
    suspend fun requestToken(
        nodeAddress: String,
        alias: String,
        displayName: String,
        pin: String?,
    ): Token
}

@JvmName("create")
fun InfinityService(client: OkHttpClient): InfinityService = OkHttpInfinityService(client)
