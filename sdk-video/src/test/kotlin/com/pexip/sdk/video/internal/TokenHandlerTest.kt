package com.pexip.sdk.video.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class TokenHandlerTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var service: TestInfinityService
    private lateinit var handler: TokenHandler

    @BeforeTest
    fun setUp() {
        service = TestInfinityService()
        handler = TokenHandler(service)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `occasionally refreshes token`() = runTest(StandardTestDispatcher()) {
        val job = handler.launchIn(this)
        service.expires.forEachIndexed { index, expires ->
            runCurrent()
            assertEquals(index + 1, service.counter.get())
            advanceTimeBy(expires.inWholeMilliseconds / 2)
        }
        job.cancelAndJoin()
        service.releaseTokenJob.join()
    }

    private class TestInfinityService : InfinityService {

        val counter = AtomicInteger(0)
        val expires = List(10) { (it + 1).minutes }
        val releaseTokenJob = Job()

        override fun events(): Flow<Event> {
            TODO("Not yet implemented")
        }

        override suspend fun refreshToken(): Duration =
            expires[counter.getAndIncrement()]

        override suspend fun releaseToken() {
            releaseTokenJob.complete()
        }
    }
}
