package com.pexip.sdk.workflow.core

import androidx.compose.runtime.Composable

/**
 * Represents a Workflow that may have [Props], [Output] and [Rendering].
 *
 * [Props] are provided by the parent of this Workflow; this Workflow will re-render if new [Props]
 * instance is supplied. Use [Unit] if the Workflow doesn't require any input from the parent.
 *
 * [Output] is used to communicate with the parent of this Workflow. Use [Nothing] if the Workflow
 * doesn't have any outputs.
 *
 * [Rendering] represents a type that may contain data to be consumed either by other Workflows
 * or UI. Use [Unit] if the Workflow doesn't offer any such type. Such a Workflow may be used
 * to de-couple certain parts of the parent Workflow, mimicking Worker API.
 */
@ExperimentalWorkflowApi
fun interface Workflow<in Props, out Output, out Rendering> {

    /**
     * Renders this Workflow with the provided [Props] and [Output] callback.
     *
     * @param props an input to this Workflow
     * @param onOutput a callback to communicate with the parent of this Workflow
     * @return a representation of this Workflow's internal state and/or ways to modify it
     */
    @ExperimentalWorkflowApi
    @Composable
    fun render(props: Props, onOutput: (Output) -> Unit): Rendering
}

/**
 * A shortcut for a prop-less, output-less [Workflow].
 *
 * @see Workflow.render
 */
@ExperimentalWorkflowApi
@Composable
fun <Rendering> Workflow<Unit, Nothing, Rendering>.render(): Rendering = render(Unit)

/**
 * A shortcut for an output-less [Workflow].
 *
 * @see Workflow.render
 */
@ExperimentalWorkflowApi
@Composable
fun <Props, Rendering> Workflow<Props, Nothing, Rendering>.render(props: Props): Rendering =
    render(props) { }

/**
 * A shortcut for a prop-less [Workflow].
 *
 * @see Workflow.render
 */
@ExperimentalWorkflowApi
@Composable
fun <Output, Rendering> Workflow<Unit, Output, Rendering>.render(onOutput: (Output) -> Unit): Rendering =
    render(Unit, onOutput)
