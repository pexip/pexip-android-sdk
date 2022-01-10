package com.pexip.sdk.video.node.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.minidns.hla.ResolverApi
import org.minidns.record.A

internal class MiniDnsNodeResolver(private val api: ResolverApi) : NodeResolver {

    constructor() : this(ResolverApi.INSTANCE)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun resolve(uri: String): String = withContext(Dispatchers.IO) {
        val (_, domain) = uri.split("@")
        val srvResult = api.resolveSrv("_pexapp._tcp.$domain")
        if (srvResult.wasSuccessful()) {
            srvResult.sortedSrvResolvedAddresses.first()
                .srv
                .target
                .toString()
        } else {
            val aResult = api.resolve(domain, A::class.java)
            if (aResult.wasSuccessful()) {
                aResult.answers.first().toString()
            } else {
                throw NoSuchElementException()
            }
        }
    }
}
