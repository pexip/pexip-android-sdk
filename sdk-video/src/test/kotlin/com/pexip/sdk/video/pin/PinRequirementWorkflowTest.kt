package com.pexip.sdk.video.pin

import com.pexip.sdk.video.api.PinRequirement
import com.pexip.sdk.video.api.TestInfinityService
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
    fun `outputs Some or None`() {
        val pinRequirements = setOf(
            PinRequirement.Some(Random.nextBoolean()),
            PinRequirement.None(
                token = "${Random.nextInt()}",
                expires = 120
            )
        )
        pinRequirements.forEach {
            val service = object : TestInfinityService() {

                override suspend fun getPinRequirement(
                    nodeAddress: String,
                    alias: String,
                    displayName: String,
                ): PinRequirement = it
            }
            val workflow = PinRequirementWorkflow(service)
            workflow.launchForTestingFromStartWith(props, context = dispatcher) {
                assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitNextRendering())
                val output = when (it) {
                    is PinRequirement.Some -> PinRequirementOutput.Some(it.required)
                    is PinRequirement.None -> PinRequirementOutput.None(
                        token = it.token,
                        expires = it.expires
                    )
                }
                assertEquals(output, awaitNextOutput())
            }
        }
    }

    @Test
    fun `outputs Back`() {
        val t = Throwable()
        val service = object : TestInfinityService() {

            override suspend fun getPinRequirement(
                nodeAddress: String,
                alias: String,
                displayName: String,
            ): PinRequirement {
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
