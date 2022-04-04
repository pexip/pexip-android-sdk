package com.pexip.sdk.api.infinity.internal

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * An [ExecutorService] to help with boring tasks
 */
internal object Dispatcher : ExecutorService by ThreadPoolExecutor(
    0,
    1,
    60,
    TimeUnit.SECONDS,
    LinkedBlockingQueue()
)
