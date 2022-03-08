package com.pexip.sdk.video

import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.await
import com.pexip.sdk.video.internal.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import org.minidns.hla.SrvResolverResult
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * A class that can resolve node addresses.
 */
public class NodeResolver private constructor(
    private val api: ResolverApi,
    private val client: OkHttpClient,
) {

    /**
     * Resolves the node address for the provided [host]. Implementations should consult with
     * (documentation)[https://docs.pexip.com/clients/configuring_dns_pexip_app.htm#next_gen_mobile]
     * for the recommended flow.
     *
     * @param joinDetails an alias to use to resolve the best node address
     * @return a node to connect to
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun resolve(joinDetails: JoinDetails): Node? =
        resolveSrvRecord(joinDetails) ?: resolveARecord(joinDetails)

    private suspend fun resolveSrvRecord(joinDetails: JoinDetails): Node? {
        val addresses = api.awaitSortedSrvResolvedAddresses("pexapp", "tcp", joinDetails.host)
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

    private fun SrvResolverResult.ResolvedSrvRecord.toNodeAddress() = HttpUrl.Builder()
        .scheme(if (srv.port == 443) "https" else "http")
        .host(srv.target.ace)
        .port(srv.port)
        .build()

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun resolveARecord(joinDetails: JoinDetails) = try {
        val address = withContext(Dispatchers.IO) { InetAddress.getByName(joinDetails.host) }
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun ResolverApi.awaitSortedSrvResolvedAddresses(
        service: String,
        proto: String,
        name: String,
    ) = withContext(Dispatchers.IO) {
        resolveSrv("_$service._$proto.$name")
            .takeIf { it.wasSuccessful() }
            ?.sortedSrvResolvedAddresses
            ?: emptyList()
    }

    private suspend fun OkHttpClient.isInMaintenanceMode(nodeAddress: HttpUrl): Boolean {
        val response = await {
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
