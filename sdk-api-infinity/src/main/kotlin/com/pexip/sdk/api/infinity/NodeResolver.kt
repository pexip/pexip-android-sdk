package com.pexip.sdk.api.infinity

import android.content.Context
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.internal.RealNodeResolver
import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import java.net.URL

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
    public fun resolve(host: String): Call<List<URL>>

    public companion object {

        @JvmStatic
        public fun initialize(context: Context) {
            AndroidUsingLinkProperties.setup(context.applicationContext)
        }

        @JvmStatic
        @JvmOverloads
        public fun create(dnssec: Boolean = false): NodeResolver =
            RealNodeResolver(if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE)
    }
}
