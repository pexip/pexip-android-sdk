package com.pexip.sdk.video.internal

import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.api.InfinityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.minidns.hla.ResolverApi
import org.minidns.record.A

internal class MiniDnsNodeResolver(
    private val api: ResolverApi,
    private val service: InfinityService,
) : NodeResolver {

    constructor() : this(ResolverApi.INSTANCE, InfinityService)

    override suspend fun resolve(host: String): String? =
        resolveSrvRecord(host) ?: resolveARecord(host)

    private suspend fun resolveSrvRecord(host: String): String? {
        val r = api.awaitSrv(SERVICE, PROTO, host).takeIf { it.wasSuccessful() } ?: return null
        return r.sortedSrvResolvedAddresses
            .asSequence()
            .map { "$SCHEME://${it.srv.target}" }
            .firstOrNull { !service.isInMaintenanceMode(it) }
    }

    private suspend fun resolveARecord(host: String) = api.awaitA(host)
        .takeIf { it.wasSuccessful() }
        ?.let { "$SCHEME://${it.answers.first()}" }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun ResolverApi.awaitSrv(service: String, proto: String, name: String) =
        withContext(Dispatchers.IO) {
            resolveSrv("_$service._$proto.$name")
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun ResolverApi.awaitA(host: String) = withContext(Dispatchers.IO) {
        resolve(host, A::class.java)
    }

    private companion object {

        const val SERVICE = "pexapp"
        const val PROTO = "tcp"
        const val SCHEME = "https"
    }
}
