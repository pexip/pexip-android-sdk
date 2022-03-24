package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.Call
import com.pexip.sdk.video.api.Callback
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.NodeResolver
import okhttp3.HttpUrl
import org.minidns.hla.ResolverApi
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class RealNodeResolver(private val api: ResolverApi) : NodeResolver {

    override fun resolve(host: String): Call<List<Node>> = ResolveCall(api, host)

    private class ResolveCall(
        private val api: ResolverApi,
        private val host: String,
    ) : Call<List<Node>> {

        private val executed = AtomicBoolean()
        private var future = AtomicReference<Future<*>?>()

        override fun execute(): List<Node> = maybeExecute { resolve(host) }

        override fun enqueue(callback: Callback<List<Node>>) = maybeExecute {
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
            future.getAndUpdate {
                it?.cancel(true)
                null
            }
        }

        private inline fun <T> maybeExecute(block: () -> T) =
            when (executed.compareAndSet(false, true)) {
                true -> block()
                else -> throw IllegalStateException()
            }

        private fun resolve(host: String) = resolveSrvRecords(host).ifEmpty { resolveARecord(host) }

        private fun resolveSrvRecords(host: String): List<Node> {
            val addresses = api.resolveSrv("pexapp", "tcp", host)
                ?.sortedSrvResolvedAddresses
                ?: emptyList()
            return addresses.map { address ->
                Node {
                    scheme(if (address.srv.port == 443) "https" else "http")
                    host(address.srv.target.ace)
                    port(address.srv.port)
                }
            }
        }

        private fun resolveARecord(host: String): List<Node> = try {
            val address = InetAddress.getByName(host)
            val node = Node {
                scheme("https")
                host(address.hostName)
            }
            listOf(node)
        } catch (e: UnknownHostException) {
            emptyList()
        }

        private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
            resolveSrv("_$service._$proto.$name").takeIf { it.wasSuccessful() }

        private inline fun Node(block: HttpUrl.Builder.() -> Unit) =
            Node(HttpUrl.Builder().apply(block).build())
    }
}
