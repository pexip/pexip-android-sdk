package com.pexip.sdk.video.pin

import com.pexip.sdk.video.api.TestInfinityService
import com.pexip.sdk.video.api.Token
import com.pexip.sdk.video.api.internal.InvalidPinException
import com.pexip.sdk.video.nextConferenceAlias
import com.pexip.sdk.video.nextPin
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.test.test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalWorkflowApi::class)
@RunWith(RobolectricTestRunner::class)
class PinChallengeWorkflowTest {

    private lateinit var props: PinChallengeProps

    @BeforeTest
    fun setUp() {
        props = PinChallengeProps(
            nodeAddress = "localhost",
            conferenceAlias = Random.nextConferenceAlias(),
            displayName = "John",
            required = true
        )
    }

    @Test
    fun `outputs Back`() {
        val service = TestInfinityService()
        val workflow = PinChallengeWorkflow(service)
        workflow.test(props) {
            val rendering = awaitRendering()
            rendering.onBackClick()
            assertEquals(PinChallengeOutput.Back, awaitOutput())
        }
    }

    @Test
    fun `outputs Token`() {
        val token = Token(
            token = "${Random.nextInt()}",
            expires = 120
        )
        val service = object : TestInfinityService() {

            override suspend fun requestToken(
                nodeAddress: String,
                conferenceAlias: String,
                displayName: String,
                pin: String,
            ): Token = token
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.test(props) {
            var rendering = awaitRendering()
            assertEquals("", rendering.pin)
            assertFalse(rendering.error)
            assertFalse(rendering.submitEnabled)
            val pin = Random.nextPin()
            rendering.onPinChange(pin)
            rendering = awaitRendering()
            assertEquals(pin, rendering.pin)
            assertFalse(rendering.error)
            assertTrue(rendering.submitEnabled)
            rendering.onSubmitClick()
            assertEquals(
                expected = PinChallengeOutput.Token(
                    token = token.token,
                    expires = token.expires
                ),
                actual = awaitOutput()
            )
            rendering = awaitRendering()
            assertEquals(pin, rendering.pin)
            assertFalse(rendering.error)
            assertFalse(rendering.submitEnabled)
        }
    }

    @Test
    fun `clears input when PIN is invalid`() {
        val service = object : TestInfinityService() {

            override suspend fun requestToken(
                nodeAddress: String,
                conferenceAlias: String,
                displayName: String,
                pin: String,
            ): Token = throw InvalidPinException()
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.test(props) {
            var rendering = awaitRendering()
            rendering.onPinChange(Random.nextPin())
            rendering = awaitRendering()
            rendering.onSubmitClick()
            rendering = awaitRendering()
            assertEquals("", rendering.pin)
            assertTrue(rendering.error)
            assertFalse(rendering.submitEnabled)
        }
    }

    @Test
    fun `clears input on error`() {
        val service = object : TestInfinityService() {

            override suspend fun requestToken(
                nodeAddress: String,
                conferenceAlias: String,
                displayName: String,
                pin: String,
            ): Token = throw Throwable()
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.test(props) {
            var rendering = awaitRendering()
            val pin = Random.nextPin()
            rendering.onPinChange(pin)
            rendering = awaitRendering()
            rendering.onSubmitClick()
            rendering = awaitRendering()
            assertEquals(pin, rendering.pin)
            assertTrue(rendering.error)
            assertTrue(rendering.submitEnabled)
        }
    }
}
