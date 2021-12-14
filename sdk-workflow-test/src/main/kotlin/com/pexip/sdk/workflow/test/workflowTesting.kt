package com.pexip.sdk.workflow.test

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameMillis
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow
import com.pexip.sdk.workflow.test.internal.launchMolecule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext

/**
 * A props-less version of [Workflow.test].
 *
 * @see [Workflow.test]
 */
@ExperimentalWorkflowApi
fun <Output, Rendering> Workflow<Unit, Output, Rendering>.test(
    timeoutMs: Long = 1_000L,
    validate: suspend WorkflowTurbine<Output, Rendering>.() -> Unit,
) = test(Unit, timeoutMs, validate)

/**
 * Validates this [Workflow]'s behavior via [validate] block with the specified props:
 *
 * ```
 * HelloWorkflow.test(props = HelloProps(name = "Lewis")) {
 *    var rendering = awaitRendering()
 *    assertEquals("Hello, Lewis!", rendering.message)
 *    rendering.onBackClick()
 *    assertEquals(HelloOutput, awaitOutput())
 * }
 *
 * @param props an instance of props to use throughout the test
 * @param timeoutMs a timeout in milliseconds
 * @param validate a block that validates this Workflow behavior
 */
@ExperimentalWorkflowApi
fun <Props, Output, Rendering> Workflow<Props, Output, Rendering>.test(
    props: Props,
    timeoutMs: Long = 1_000L,
    validate: suspend WorkflowTurbine<Output, Rendering>.() -> Unit,
) = test(MutableStateFlow(props), timeoutMs, validate)

/**
 * Validates this [Workflow]'s behavior via [validate] block with the specified props:
 *
 * ```
 * val props = MutableStateFlow(HelloProps(name = "Valterri"))
 * HelloWorkflow.test(props = props) {
 *    var rendering = awaitRendering()
 *    assertEquals("Hello, Valterri!", rendering.message)
 *    props.value = "George"
 *    rendering = awaitRendering()
 *    assertEquals("Hello, George!", rendering.message)
 *    rendering.onBackClick()
 *    assertEquals(HelloOutput, awaitOutput())
 * }
 *
 * @param props a [StateFlow] of props
 * @param timeoutMs a timeout in milliseconds
 * @param validate a block that validates this Workflow behavior
 */
@ExperimentalWorkflowApi
fun <Props, Output, Rendering> Workflow<Props, Output, Rendering>.test(
    props: StateFlow<Props>,
    timeoutMs: Long = 1_000L,
    validate: suspend WorkflowTurbine<Output, Rendering>.() -> Unit,
) = runBlocking {
    val events = Channel<Event<Output, Rendering>>(Channel.UNLIMITED)
    val exceptionHandler = EventEmittingExceptionHandler(events)
    val clock = BroadcastFrameClock()

    // Ensure exceptions in the molecule do not crash runBlocking. We redirect exceptions as events.
    val workflowJob = SupervisorJob()

    launch(exceptionHandler + clock + workflowJob, start = CoroutineStart.UNDISPATCHED) {
        try {
            launchMolecule(
                emitter = { value ->
                    val result = events.trySend(Event.Rendering(value))
                    if (result.isFailure) {
                        throw AssertionError("Unable to send rendering to events channel.")
                    }
                },
                body = {
                    val currentProps by props.collectAsState()
                    render(currentProps) { output ->
                        val result = events.trySend(Event.Output(output))
                        if (result.isFailure) {
                            throw AssertionError("Unable to send output to events channel.")
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            val result = events.trySend(Event.Error(t))
            if (result.isFailure) {
                throw AssertionError("Unable to send error to events channel.")
            }
        }
    }

    try {
        val workflowTurbine = TickOnDemandWorkflowTurbine(
            events = events,
            clock = clock,
            timeoutMs = timeoutMs,
        )
        workflowTurbine.validate()
    } catch (t: Throwable) {
        while (true) {
            // If the validation lambda has thrown, search for an exception from the composable to
            // include which may help indicate the cause of the failure.
            val event = events.tryReceive().getOrNull() ?: break // No more items.
            if (event is Event.Error) {
                t.addSuppressed(event.throwable)
            }
        }
        throw t
    } finally {
        workflowJob.cancelAndJoin()
    }
}

interface WorkflowTurbine<Output, Rendering> {

    /**
     * Assert that an event was received and return it.
     * If no events have been received, this function will suspend for up to timeout.
     *
     * @throws TimeoutCancellationException if no event was received in time.
     */
    suspend fun awaitEvent(): Event<Output, Rendering>

    /**
     * Assert that the next event received was an output and return it.
     * If no events have been received, this function will suspend for up to timeout.
     *
     * @throws AssertionError if the next event was completion or an error.
     * @throws TimeoutCancellationException if no event was received in time.
     */
    suspend fun awaitOutput(): Output

    /**
     * Assert that the next event received was a rendering and return it.
     * If no events have been received, this function will suspend for up to timeout.
     *
     * @throws AssertionError if the next event was completion or an error.
     * @throws TimeoutCancellationException if no event was received in time.
     */
    suspend fun awaitRendering(): Rendering

    /**
     * Assert that the next event received was an error terminating the flow.
     * If no events have been received, this function will suspend for up to timeout.
     *
     * @throws AssertionError if the next event was an item or completion.
     * @throws TimeoutCancellationException if no event was received in time.
     */
    suspend fun awaitError(): Throwable
}

sealed class Event<out Output, out Rendering> {

    data class Output<Output>(val value: Output) : Event<Output, Nothing>() {
        override fun toString(): String = "Output($value)"
    }

    data class Rendering<Rendering>(val value: Rendering) : Event<Nothing, Rendering>() {
        override fun toString(): String = "Rendering($value)"
    }

    data class Error(val throwable: Throwable) : Event<Nothing, Nothing>() {
        override fun toString(): String = "Error(${throwable::class.simpleName})"
    }
}

private class TickOnDemandWorkflowTurbine<Output, Rendering>(
    private val events: Channel<Event<Output, Rendering>>,
    private val clock: BroadcastFrameClock,
    private val timeoutMs: Long,
) : WorkflowTurbine<Output, Rendering> {

    private suspend fun <T> withTimeout(body: suspend () -> T): T = when (timeoutMs) {
        0L -> body()
        else -> withTimeout(timeoutMs) {
            body()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun awaitEvent(): Event<Output, Rendering> = withTimeout {
        // Always yield once to give any launched coroutines a chance to execute before handing
        // control back to the caller.
        do {
            yieldAndAwaitFrame()
        } while (events.isEmpty)
        events.receive()
    }

    override suspend fun awaitOutput(): Output {
        val event = awaitEvent()
        if (event !is Event.Output<Output>) {
            unexpectedEvent(event, "rendering")
        }
        return event.value
    }

    override suspend fun awaitRendering(): Rendering {
        val event = awaitEvent()
        if (event !is Event.Rendering<Rendering>) {
            unexpectedEvent(event, "rendering")
        }
        return event.value
    }

    override suspend fun awaitError(): Throwable {
        val event = awaitEvent()
        if (event !is Event.Error) {
            unexpectedEvent(event, "error")
        }
        return event.throwable
    }

    private fun unexpectedEvent(event: Event<*, *>, expected: String): Nothing {
        val cause = (event as? Event.Error)?.throwable
        throw AssertionError("Expected $expected but found $event", cause)
    }

    private suspend fun yieldAndAwaitFrame() {
        // Work could be scheduled on the current dispatcher, so yield before advancing the clock.
        yield()
        clock.awaitFrame()
    }

    private suspend fun BroadcastFrameClock.awaitFrame() {
        // TODO Remove the need for two frames to happen!
        //  I think this is because of the diff-sender is a hot loop that immediately reschedules
        //  itself on the clock. This schedules it ahead of the coroutine which applies changes and
        //  so we need to trigger an additional frame to actually emit the change's diffs.
        repeat(2) {
            coroutineScope {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    withFrameMillis { }
                }
                sendFrame(0L)
            }
        }
    }
}

private class EventEmittingExceptionHandler<Output, Rendering>(
    private val events: Channel<Event<Output, Rendering>>,
) : CoroutineExceptionHandler {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val result = events.trySend(Event.Error(exception))
        if (result.isFailure) {
            throw AssertionError("Unable to send error to events channel.")
        }
    }

    override val key get() = CoroutineExceptionHandler
}
