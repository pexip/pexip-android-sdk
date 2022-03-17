package com.pexip.sdk.video.conference.internal

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

internal class DirectExecutorService : AbstractExecutorService() {

    @Volatile
    private var shutdown = false

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun shutdown() {
        shutdown = true
    }

    override fun shutdownNow(): List<Runnable> {
        shutdown()
        return emptyList()
    }

    override fun isShutdown(): Boolean = shutdown

    override fun isTerminated(): Boolean = shutdown

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean = true
}
