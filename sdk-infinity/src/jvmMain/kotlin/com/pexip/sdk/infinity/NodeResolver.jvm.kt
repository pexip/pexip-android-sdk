/*
 * Copyright 2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.infinity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.minidns.hla.DnssecResolverApi
import org.minidns.hla.ResolverApi
import org.minidns.hla.SrvResolverResult
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Creates a new instance of [NodeResolver] backed by [ResolverApi].
 *
 * @param dnssec whether to use DNSSEC
 */
public actual fun NodeResolver.Companion.create(dnssec: Boolean): NodeResolver =
    NodeResolver.create(if (dnssec) DnssecResolverApi.INSTANCE else ResolverApi.INSTANCE)

/**
 * Creates a new instance of [NodeResolver] backed by [ResolverApi].
 *
 * @param api an instance of [ResolverApi]
 */
public fun NodeResolver.Companion.create(api: ResolverApi): NodeResolver =
    object : NodeResolver {

        override suspend fun resolve(host: String): Nodes? {
            require(host.isNotBlank()) { "host is blank." }
            val srv = resolveSrvRecords(host)
            if (srv.isNotEmpty()) return Nodes.Srv(srv)
            val a = resolveARecord(host)
            if (a != null) return Nodes.A(a)
            return null
        }

        private suspend fun resolveSrvRecords(host: String) = runInterruptible(Dispatchers.IO) {
            api.resolveSrv("_pexapp._tcp.$host")
                ?.takeIf(SrvResolverResult::wasSuccessful)
                ?.sortedSrvResolvedAddresses
                ?.map { Node(it.srv.target.ace, it.srv.port) }
                ?: emptyList()
        }

        private suspend fun resolveARecord(host: String) = runInterruptible(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(host)
                Node(address.hostName)
            } catch (e: UnknownHostException) {
                null
            }
        }
    }
