package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.RealNodeResolver
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi

/**
 * A class that can resolve node addresses.
 */
public fun interface NodeResolver {

    /**
     * Resolves the node address for the provided [host]. Clients should consult with
     * (documentation)[https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile]
     * for the recommended flow.
     *
     * @param host a host to use to resolve node addresses
     * @return a [Call]
     */
    public fun resolve(host: String): Call<List<Node>>

    public companion object {

        @JvmStatic
        @JvmOverloads
        public fun create(dnssec: Boolean = false): NodeResolver =
            RealNodeResolver(if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE)
    }
}
