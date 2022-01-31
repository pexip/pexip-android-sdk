package com.pexip.sdk.video.internal

import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.api.InfinityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.minidns.hla.ResolverApi
import org.minidns.hla.SrvResolverResult
import org.minidns.record.A
import java.io.IOException
import java.net.UnknownHostException

internal class MiniDnsNodeResolver(
    private val api: ResolverApi,
    private val service: InfinityService,
) : NodeResolver {

    constructor() : this(ResolverApi.INSTANCE, InfinityService)

    override suspend fun resolve(host: String): String? =
        resolveSrvRecord(host) ?: resolveARecord(host)

    private suspend fun resolveSrvRecord(host: String): String? {
        val addresses = api.awaitSortedSrvResolvedAddresses(SERVICE, PROTO, host)
        for (address in addresses) {
            val nodeAddress = address.toNodeAddress()
            try {
                return nodeAddress.takeUnless { service.isInMaintenanceMode(it) }
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

    private fun SrvResolverResult.ResolvedSrvRecord.toNodeAddress() = buildString {
        val port = srv.port
        append(if (port == PORT_HTTPS) SCHEME_HTTPS else SCHEME_HTTP)
        append("://")
        append(srv.target.ace)
        if (port == PORT_HTTP || port == PORT_HTTPS) return@buildString
        append(":")
        append(port)
    }

    private suspend fun resolveARecord(host: String): String? {
        val result = api.awaitA(host) ?: return null
        val nodeAddress = buildString {
            append(SCHEME_HTTPS)
            append("://")
            append(result.question.name.ace)
        }
        return try {
            nodeAddress.takeUnless { service.isInMaintenanceMode(it) }
        } catch (e: UnknownHostException) {
            null
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            null
        }
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun ResolverApi.awaitA(host: String) = withContext(Dispatchers.IO) {
        resolve(host, A::class.java).takeIf { it.wasSuccessful() }
    }

    private companion object {

        const val SERVICE = "pexapp"
        const val PROTO = "tcp"
        const val PORT_HTTP = 80
        const val PORT_HTTPS = 443
        const val SCHEME_HTTP = "http"
        const val SCHEME_HTTPS = "https"
    }
}
