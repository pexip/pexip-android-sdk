package com.pexip.sdk.video.coroutines

import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.Node
import com.pexip.sdk.video.NodeResolver
import kotlinx.coroutines.cancelFutureOnCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public suspend fun NodeResolver.resolve(details: JoinDetails): Node? = suspendCancellableCoroutine {
    val callback = object : NodeResolver.Callback {

        override fun onSuccess(resolver: NodeResolver, node: Node?) {
            it.resume(node)
        }

        override fun onFailure(resolver: NodeResolver, t: Throwable) {
            it.resumeWithException(t)
        }
    }
    it.cancelFutureOnCancellation(resolve(details, callback))
}
