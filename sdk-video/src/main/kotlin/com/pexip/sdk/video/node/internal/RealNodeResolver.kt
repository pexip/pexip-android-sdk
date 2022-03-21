package com.pexip.sdk.video.node.internal

import com.pexip.sdk.video.internal.Dispatcher
import com.pexip.sdk.video.internal.execute
import com.pexip.sdk.video.internal.url
import com.pexip.sdk.video.node.Node
import com.pexip.sdk.video.node.NodeResolver
import com.pexip.sdk.video.token.NoSuchNodeException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.minidns.hla.ResolverApi
import org.minidns.hla.SrvResolverResult
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Future

internal class RealNodeResolver(private val api: ResolverApi, private val client: OkHttpClient) :
    NodeResolver {

    override fun resolve(host: String, callback: NodeResolver.Callback): Future<*> {
        val runnable = ResolveRunnable(this, host, callback)
        return Dispatcher.submit(runnable)
    }

    private fun resolveSrvRecord(host: String): Node? {
        val addresses = api.resolveSrv("pexapp", "tcp", host)
            ?.sortedSrvResolvedAddresses
            ?: emptyList()
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

    private fun resolveARecord(host: String): Node? = try {
        val address = InetAddress.getByName(host)
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

    private fun SrvResolverResult.ResolvedSrvRecord.toNodeAddress() = HttpUrl.Builder()
        .scheme(if (srv.port == 443) "https" else "http")
        .host(srv.target.ace)
        .port(srv.port)
        .build()

    private fun ResolverApi.resolveSrv(service: String, proto: String, name: String) =
        resolveSrv("_$service._$proto.$name").takeIf { it.wasSuccessful() }

    private fun OkHttpClient.isInMaintenanceMode(nodeAddress: HttpUrl): Boolean {
        val response = execute {
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
