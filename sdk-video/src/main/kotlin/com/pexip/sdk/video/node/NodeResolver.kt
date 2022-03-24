package com.pexip.sdk.video.node

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.node.internal.RealNodeResolver
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import java.util.concurrent.Future

/**
 * A class that can resolve node addresses.
 */
public fun interface NodeResolver {

    /**
     * A callback that will be invoked after call to [resolve].
     */
    public interface Callback {

        /**
         * Invoked when node resolution completed without issues.
         *
         * @param resolver a [NodeResolver] used to perform this operation
         * @param node an instance of [Node], or null
         */
        public fun onSuccess(resolver: NodeResolver, node: Node?)

        /**
         * Invoked when node resolution encountered an error.
         *
         * @param resolver a [NodeResolver] used to perform this operation
         * @param t an error
         */
        public fun onFailure(resolver: NodeResolver, t: Throwable)
    }

    /**
     * Resolves the node address for the provided [host]. Implementations should consult with
     * (documentation)[https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile]
     * for the recommended flow.
     *
     * @param host a host to use to resolve the best node address
     * @param callback a completion handler
     * @return a [Future] that may be used to cancel the operation
     */
    public fun resolve(host: String, callback: Callback): Future<*>

    public companion object {

        @JvmStatic
        @JvmOverloads
        public fun create(
            service: InfinityService,
            dnssec: Boolean = false,
        ): NodeResolver = RealNodeResolver(
            service = service,
            api = if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE,
        )
    }
}
