package com.pexip.sdk.video.pin

import com.pexip.sdk.video.api.TestInfinityService
import com.pexip.sdk.video.api.Token
import com.pexip.sdk.video.api.internal.InvalidPinException
import com.pexip.sdk.video.nextAlias
import com.pexip.sdk.video.nextPin
import com.squareup.workflow1.testing.launchForTestingFromStartWith
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PinChallengeWorkflowTest {

    private lateinit var props: PinChallengeProps

    @BeforeTest
    fun setUp() {
        props = PinChallengeProps(
            nodeAddress = "localhost",
            alias = Random.nextAlias(),
            displayName = "John",
            required = true
        )
    }

    @Test
    fun `outputs Back`() {
        val service = TestInfinityService()
        val workflow = PinChallengeWorkflow(service)
        workflow.launchForTestingFromStartWith(props) {
            val rendering = awaitNextRendering()
            rendering.onBackClick()
            assertEquals(PinChallengeOutput.Back, awaitNextOutput())
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
                alias: String,
                displayName: String,
                pin: String,
            ): Token = token
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.launchForTestingFromStartWith(props) {
            var rendering = awaitNextRendering()
            assertEquals("", rendering.pin)
            assertFalse(rendering.error)
            assertFalse(rendering.submitEnabled)
            val pin = Random.nextPin()
            rendering.onPinChange(pin)
            rendering = awaitNextRendering()
            assertEquals(pin, rendering.pin)
            assertFalse(rendering.error)
            assertTrue(rendering.submitEnabled)
            rendering.onSubmitClick()
            assertEquals(
                expected = PinChallengeOutput.Token(
                    token = token.token,
                    expires = token.expires
                ),
                actual = awaitNextOutput()
            )
            rendering = awaitNextRendering()
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
                alias: String,
                displayName: String,
                pin: String,
            ): Token = throw InvalidPinException()
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.launchForTestingFromStartWith(props) {
            var rendering = awaitNextRendering()
            rendering.onPinChange(Random.nextPin())
            rendering = awaitNextRendering()
            rendering.onSubmitClick()
            rendering = awaitNextRendering()
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
                alias: String,
                displayName: String,
                pin: String,
            ): Token = throw Throwable()
        }
        val workflow = PinChallengeWorkflow(service)
        workflow.launchForTestingFromStartWith(props) {
            var rendering = awaitNextRendering()
            val pin = Random.nextPin()
            rendering.onPinChange(pin)
            rendering = awaitNextRendering()
            rendering.onSubmitClick()
            rendering = awaitNextRendering()
            assertEquals(pin, rendering.pin)
            assertTrue(rendering.error)
            assertTrue(rendering.submitEnabled)
        }
    }
}
