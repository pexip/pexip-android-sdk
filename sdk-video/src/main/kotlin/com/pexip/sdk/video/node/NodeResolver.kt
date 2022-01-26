package com.pexip.sdk.video.node

import com.pexip.sdk.video.node.internal.MiniDnsNodeResolver

fun interface NodeResolver {

    suspend fun resolve(host: String): String

    companion object : NodeResolver by MiniDnsNodeResolver()
}
