package com.pexip.sdk.video.sample

import com.squareup.workflow1.BaseRenderContext
import com.squareup.workflow1.WorkflowAction

fun <Props, State, Output> BaseRenderContext<Props, State, Output>.send(action: () -> WorkflowAction<Props, State, Output>): () -> Unit =
    { actionSink.send(action()) }

fun <Props, State, Output, T> BaseRenderContext<Props, State, Output>.send(action: (T) -> WorkflowAction<Props, State, Output>): (T) -> Unit =
    { actionSink.send(action(it)) }
