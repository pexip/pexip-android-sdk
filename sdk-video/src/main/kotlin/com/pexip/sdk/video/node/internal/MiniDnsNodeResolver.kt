package com.pexip.sdk.video.node.internal

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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun resolve(uri: String): String = withContext(Dispatchers.IO) {
        val (_, domain) = uri.split("@")
        val srvResult = api.resolveSrv(SERVICE, PROTO, domain)
        if (srvResult.wasSuccessful()) {
            srvResult.sortedSrvResolvedAddresses
                .asSequence()
                .map { "$SCHEME://${it.srv.target}" }
                .first { !service.isInMaintenanceMode(it) }
        } else {
            val aResult = api.resolve(domain, A::class.java)
            if (aResult.wasSuccessful()) {
                "$SCHEME://${aResult.answers.first()}"
            } else {
                throw NoSuchElementException()
            }
        }
    }

    private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
        resolveSrv("_$service._$proto.$name")

    private companion object {

        const val SERVICE = "pexapp"
        const val PROTO = "tcp"
        const val SCHEME = "https"
    }
}
