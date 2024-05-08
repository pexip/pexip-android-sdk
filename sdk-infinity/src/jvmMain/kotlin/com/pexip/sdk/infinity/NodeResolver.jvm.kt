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

        override suspend fun resolve(host: String): List<Node> {
            require(host.isNotBlank()) { "host is blank." }
            return runInterruptible(Dispatchers.IO) {
                resolveSrvRecords(host).ifEmpty { resolveARecord(host) }
            }
        }

        private fun resolveSrvRecords(host: String): List<Node> {
            val result = api.resolveSrv("_pexapp._tcp.$host")
            if (!result.wasSuccessful()) return emptyList()
            return result.sortedSrvResolvedAddresses.map { Node(it.srv.target.ace, it.srv.port) }
        }

        private fun resolveARecord(host: String) = try {
            val address = InetAddress.getByName(host)
            listOf(Node(address.hostName))
        } catch (e: UnknownHostException) {
            emptyList()
        }
    }
