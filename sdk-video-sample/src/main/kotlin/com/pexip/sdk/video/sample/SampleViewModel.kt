package com.pexip.sdk.video.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SampleViewModel(handle: SavedStateHandle) : ViewModel() {

    private val _output = MutableSharedFlow<SampleOutput>(extraBufferCapacity = 1)

    val output = _output.asSharedFlow()
    val rendering by lazy {
        renderWorkflowIn(
            workflow = WorkflowComponent.sampleWorkflow,
            scope = viewModelScope,
            prop = SampleProps("Pexip Video SDK"),
            savedStateHandle = handle,
            onOutput = _output::emit
        )
    }
}
