@file:JvmName("NodeResolverFactory")

package com.pexip.sdk.video

import com.pexip.sdk.video.internal.MiniDnsNodeResolver
import java.io.IOException

/**
 * A class that can resolve node addresses.
 */
fun interface NodeResolver {

    /**
     * Resolves the node address for the provided [host].
     *
     * @param host a host to use to resolve the best node address (e.g. example.com)
     * @return a node address in the form of https://example.com or null if node was not found
     * @throws IOException if a network error was encountered during operation
     */
    suspend fun resolve(host: String): String?
}

/**
 * Creates an instance of [NodeResolver] with default implementation.
 */
@JvmName("create")
fun NodeResolver(): NodeResolver = MiniDnsNodeResolver()
