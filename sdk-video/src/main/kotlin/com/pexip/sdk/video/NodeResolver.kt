@file:JvmName("NodeResolverFactory")

package com.pexip.sdk.video

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.internal.MiniDnsNodeResolver
import org.minidns.hla.ResolverApi
import java.io.IOException

/**
 * A class that can resolve node addresses.
 */
fun interface NodeResolver {

    /**
     * Resolves the node address for the provided [host]. Implementations should consult with
     * (documentation)[https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile]
     * for the recommended flow.
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
fun NodeResolver(service: InfinityService): NodeResolver = MiniDnsNodeResolver(
    api = ResolverApi.INSTANCE,
    service = service
)
