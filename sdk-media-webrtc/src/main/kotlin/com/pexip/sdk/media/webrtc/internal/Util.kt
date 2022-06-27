package com.pexip.sdk.media.webrtc.internal

import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

internal fun Executor.maybeExecute(block: () -> Unit) = try {
    execute(block)
} catch (e: RejectedExecutionException) {
    // noop
}
