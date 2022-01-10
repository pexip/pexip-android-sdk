package com.pexip.sdk.video.pin

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.PinRequirement
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.test.test
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalWorkflowApi::class)
@RunWith(RobolectricTestRunner::class)
class PinRequirementWorkflowTest {

    private lateinit var props: PinRequirementProps

    @BeforeTest
    fun setUp() {
        props = PinRequirementProps(
            nodeAddress = "localhost",
            conferenceAlias = "${Random.nextInt(1000, 9999)}",
            displayName = "John"
        )
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
            val service = object : InfinityService {

                override suspend fun getPinRequirement(
                    nodeAddress: String,
                    conferenceAlias: String,
                    displayName: String,
                ): PinRequirement = it
            }
            val workflow = PinRequirementWorkflow(service)
            workflow.test(props) {
                assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitRendering())
                val output = when (it) {
                    is PinRequirement.Some -> PinRequirementOutput.Some(it.required)
                    is PinRequirement.None -> PinRequirementOutput.None(
                        token = it.token,
                        expires = it.expires
                    )
                }
                assertEquals(output, awaitOutput())
            }
        }
    }

    @Test
    fun `outputs Back`() {
        val t = Throwable()
        val service = object : InfinityService {

            override suspend fun getPinRequirement(
                nodeAddress: String,
                conferenceAlias: String,
                displayName: String,
            ): PinRequirement = throw t
        }
        val workflow = PinRequirementWorkflow(service)
        workflow.test(props) {
            assertEquals(PinRequirementRendering.ResolvingPinRequirement, awaitRendering())
            val rendering = assertIs<PinRequirementRendering.Failure>(awaitRendering())
            assertEquals(t, rendering.t)
            rendering.onBackClick()
            assertEquals(PinRequirementOutput.Back, awaitOutput())
        }
    }
}
