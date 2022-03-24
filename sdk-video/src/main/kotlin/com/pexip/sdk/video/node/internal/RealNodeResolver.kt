package com.pexip.sdk.video.node.internal

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.internal.Dispatcher
import com.pexip.sdk.video.node.NodeResolver
import okhttp3.HttpUrl
import org.minidns.hla.ResolverApi
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Future

internal class RealNodeResolver(
    private val service: InfinityService,
    private val api: ResolverApi,
) : NodeResolver {

    override fun resolve(host: String, callback: NodeResolver.Callback): Future<*> {
        val runnable = ResolveRunnable(this, host, callback)
        return Dispatcher.submit(runnable)
    }

    private fun resolveSrvRecord(host: String): Node? {
        val addresses = api.resolveSrv("pexapp", "tcp", host)
            ?.sortedSrvResolvedAddresses
            ?: emptyList()
        for (address in addresses) {
            val node = Node {
                scheme(if (address.srv.port == 443) "https" else "http")
                host(address.srv.target.ace)
                port(address.srv.port)
            }
            try {
                return node.takeUnless { service.newRequest(it).status().execute() }
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

    private fun resolveARecord(host: String): Node? = try {
        val address = InetAddress.getByName(host)
        val node = Node {
            scheme("https")
            host(address.hostName)
        }
        node.takeUnless { service.newRequest(it).status().execute() }
    } catch (e: UnknownHostException) {
        null
    } catch (e: IOException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
        resolveSrv("_$service._$proto.$name").takeIf { it.wasSuccessful() }

    private inline fun Node(block: HttpUrl.Builder.() -> Unit) =
        Node(HttpUrl.Builder().apply(block).build())

    private class ResolveRunnable(
        private val resolver: RealNodeResolver,
        private val host: String,
        private val callback: NodeResolver.Callback,
    ) : Runnable {

        override fun run() = try {
            val node = resolver.resolveSrvRecord(host) ?: resolver.resolveARecord(host)
            callback.onSuccess(resolver, node)
        } catch (t: Throwable) {
            callback.onFailure(resolver, t)
        }
    }
}
