package com.pexip.sdk.video.node.coroutines

import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.node.NodeResolver
import kotlinx.coroutines.cancelFutureOnCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public suspend fun NodeResolver.resolve(host: String): Node? = suspendCancellableCoroutine {
    val callback = object : NodeResolver.Callback {

        override fun onSuccess(resolver: NodeResolver, node: Node?) {
            it.resume(node)
        }

        override fun onFailure(resolver: NodeResolver, t: Throwable) {
            it.resumeWithException(t)
        }
    }
    it.cancelFutureOnCancellation(resolve(host, callback))
}
