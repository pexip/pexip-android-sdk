package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.internal.RealNodeResolver
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import java.net.URL

/**
 * A class that can resolve node addresses.
 */
public fun interface NodeResolver {

    /**
     * Resolves the node address for the provided [host]. Clients should consult with
     * [documentation](https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile)
     * for the recommended flow.
     *
     * @param host a host to use to resolve node addresses
     * @return a [Call]
     */
    public fun resolve(host: String): Call<List<URL>>

    public companion object {

        @Deprecated(
            message = "Use AndroidUsingLinkProperties.setup(context) in org.minidns:minidns-android21 instead.",
            level = DeprecationLevel.ERROR
        )
        @JvmStatic
        public fun initialize(context: Any) {
        }

        @JvmStatic
        @JvmOverloads
        public fun create(dnssec: Boolean = false): NodeResolver =
            create(if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE)

        @JvmStatic
        public fun create(api: ResolverApi): NodeResolver = RealNodeResolver(api)
    }
}
