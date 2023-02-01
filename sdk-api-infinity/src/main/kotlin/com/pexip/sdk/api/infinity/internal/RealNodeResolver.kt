/*
 * Copyright 2022 Pexip AS
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
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.infinity.NodeResolver
import org.minidns.hla.ResolverApi
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class RealNodeResolver(private val api: ResolverApi) : NodeResolver {

    override fun resolve(host: String): Call<List<URL>> = ResolveCall(api, host)

    private class ResolveCall(
        private val api: ResolverApi,
        private val host: String,
    ) : Call<List<URL>> {

        private val executed = AtomicBoolean()
        private var future = AtomicReference<Future<*>?>()

        override fun execute(): List<URL> = maybeExecute { resolve(host) }

        override fun enqueue(callback: Callback<List<URL>>) = maybeExecute {
            val f = Dispatcher.submit {
                try {
                    callback.onSuccess(this, resolve(host))
                } catch (t: Throwable) {
                    callback.onFailure(this, t)
                }
            }
            future.set(f)
        }

        override fun cancel() {
            future.getAndSet(null)?.cancel(true)
        }

        private inline fun <T> maybeExecute(block: () -> T) =
            when (executed.compareAndSet(false, true)) {
                true -> block()
                else -> throw IllegalStateException()
            }

        private fun resolve(host: String) = resolveSrvRecords(host).ifEmpty { resolveARecord(host) }

        private fun resolveSrvRecords(host: String): List<URL> {
            val addresses = api.resolveSrv("pexapp", "tcp", host)
                ?.sortedSrvResolvedAddresses
                ?: emptyList()
            return addresses.map { address ->
                URL(
                    if (address.srv.port == 443) "https" else "http",
                    address.srv.target.ace,
                    address.srv.port,
                    "",
                )
            }
        }

        private fun resolveARecord(host: String): List<URL> = try {
            val address = InetAddress.getByName(host)
            val node = URL("https", address.hostName, "")
            listOf(node)
        } catch (e: UnknownHostException) {
            emptyList()
        }

        private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
            resolveSrv("_$service._$proto.$name").takeIf { it.wasSuccessful() }
    }
}
