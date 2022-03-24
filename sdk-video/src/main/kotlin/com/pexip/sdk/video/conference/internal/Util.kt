package com.pexip.sdk.video.conference.internal

import java.util.concurrent.ExecutorService

internal fun ExecutorService.maybeSubmit(task: Runnable) = when (isShutdown) {
    true -> null
    else -> submit(task)
}
