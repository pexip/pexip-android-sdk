package com.pexip.sdk.video.node.internal

internal fun interface NodeResolver {

    suspend fun resolve(host: String): String
}
