package com.pexip.sdk.conference.infinity.internal

import java.util.concurrent.ExecutorService

internal fun ExecutorService.maybeSubmit(task: Runnable) = when (isShutdown) {
    true -> null
    else -> submit(task)
}
