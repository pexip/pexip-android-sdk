package com.pexip.sdk.video.pin

import com.pexip.sdk.video.api.RequiredPinException
import com.pexip.sdk.video.api.TestInfinityService
import com.pexip.sdk.video.api.Token
import com.squareup.workflow1.testing.launchForTestingFromStartWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class PinRequirementWorkflowTest {

    private lateinit var dispatcher: TestCoroutineDispatcher
    private lateinit var props: PinRequirementProps

    @BeforeTest
    fun setUp() {
        dispatcher = TestCoroutineDispatcher()
        props = PinRequirementProps(
            nodeAddress = "localhost",
            alias = "${Random.nextInt(1000, 9999)}",
            displayName = "John"
        )
    }

    @AfterTest
    fun tearDown() {
        dispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `outputs None`() {
        val token = Token(token = "${Random.nextInt()}", expires = 120)
        val service = object : TestInfinityService() {
            override suspend fun requestToken(
                nodeAddress: String,
                alias: String,
                displayName: String,
                pin: String?,
            ): Token = token
        }
        val workflow = PinRequirementWorkflow(service)
        workflow.launchForTestingFromStartWith(props, context = dispatcher) {
            assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitNextRendering())
            val output = PinRequirementOutput.None(
                token = token.token,
                expires = token.expires
            )
            assertEquals(output, awaitNextOutput())
        }
    }

    @Test
    fun `outputs Some`() {
        val required = Random.nextBoolean()
        val service = object : TestInfinityService() {
            override suspend fun requestToken(
                nodeAddress: String,
                alias: String,
                displayName: String,
                pin: String?,
            ): Token = throw RequiredPinException(required)
        }
        val workflow = PinRequirementWorkflow(service)
        workflow.launchForTestingFromStartWith(props, context = dispatcher) {
            assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitNextRendering())
            val output = PinRequirementOutput.Some(required)
            assertEquals(output, awaitNextOutput())
        }
    }

    @Test
    fun `outputs Back`() {
        val t = Throwable()
        val service = object : TestInfinityService() {
            override suspend fun requestToken(
                nodeAddress: String,
                alias: String,
                displayName: String,
                pin: String?,
            ): Token {
                delay(100)
                throw t
            }
        }
        val workflow = PinRequirementWorkflow(service)
        workflow.launchForTestingFromStartWith(props, context = dispatcher) {
            assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitNextRendering())
            dispatcher.advanceTimeBy(100)
            val rendering = assertIs<PinRequirementRendering.Failure>(awaitNextRendering())
            assertEquals(t, rendering.t)
            rendering.onBackClick()
            assertEquals(PinRequirementOutput.Back, awaitNextOutput())
        }
    }
}
