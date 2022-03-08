package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Dispatcher
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.execute
import com.pexip.sdk.video.internal.url
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import org.minidns.hla.SrvResolverResult
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Future

/**
 * A class that can resolve node addresses.
 */
public class NodeResolver private constructor(
    private val api: ResolverApi,
    private val client: OkHttpClient,
) {

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
     * Resolves the node address for the provided [details]. Implementations should consult with
     * (documentation)[https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile]
     * for the recommended flow.
     *
     * @param details an alias to use to resolve the best node address
     * @param callback a completion handler
     */
    public fun resolve(details: JoinDetails, callback: Callback): Future<*> {
        val runnable = ResolveRunnable(this, details, callback)
        return Dispatcher.submit(runnable)
    }

    private fun resolveSrvRecord(joinDetails: JoinDetails): Node? {
        val addresses = api.resolveSrv("pexapp", "tcp", joinDetails.host)
            ?.sortedSrvResolvedAddresses
            ?: emptyList()
        for (address in addresses) {
            val nodeAddress = address.toNodeAddress()
            try {
                return nodeAddress.takeUnless { client.isInMaintenanceMode(it) }?.let(::Node)
            } catch (e: UnknownHostException) {
                continue
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun resolveARecord(joinDetails: JoinDetails): Node? = try {
        val address = InetAddress.getByName(joinDetails.host)
        val nodeAddress = HttpUrl.Builder()
            .scheme("https")
            .host(address.hostName)
            .build()
        nodeAddress.takeUnless { client.isInMaintenanceMode(it) }?.let(::Node)
    } catch (e: UnknownHostException) {
        null
    } catch (e: IOException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun SrvResolverResult.ResolvedSrvRecord.toNodeAddress() = HttpUrl.Builder()
        .scheme(if (srv.port == 443) "https" else "http")
        .host(srv.target.ace)
        .port(srv.port)
        .build()

    private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
        resolveSrv("_$service._$proto.$name").takeIf { it.wasSuccessful() }

    private fun OkHttpClient.isInMaintenanceMode(nodeAddress: HttpUrl): Boolean {
        val response = execute {
            get()
            url(nodeAddress) { addPathSegments("api/client/v2/status") }
        }
        return response.use {
            when (it.code) {
                200 -> false
                404 -> throw NoSuchNodeException()
                503 -> true
                else -> throw IllegalStateException()
            }
        }
    }

    private class ResolveRunnable(
        private val resolver: NodeResolver,
        private val details: JoinDetails,
        private val callback: Callback,
    ) : Runnable {

        override fun run() = try {
            val node = resolver.resolveSrvRecord(details) ?: resolver.resolveARecord(details)
            callback.onSuccess(resolver, node)
        } catch (t: Throwable) {
            callback.onFailure(resolver, t)
        }
    }

    public class Builder {

        private var dnssec: Boolean = false
        private var client: OkHttpClient? = null

        /**
         * Sets whether DNSSEC should be used to resolve node address.
         *
         * DNSSEC will not be used by default.
         *
         * @param dnssec true if DNSSEC should be used, false otherwise
         * @return this [Builder]
         */
        public fun dnssec(dnssec: Boolean): Builder = apply {
            this.dnssec = dnssec
        }

        /**
         * Sets the [OkHttpClient] used to make the calls.
         *
         * @param client an instance of [OkHttpClient]
         * @return this [Builder]
         */
        public fun client(client: OkHttpClient): Builder = apply {
            this.client = client
        }

        public fun build(): NodeResolver = NodeResolver(
            api = if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE,
            client = client ?: OkHttpClient
        )
    }
}
