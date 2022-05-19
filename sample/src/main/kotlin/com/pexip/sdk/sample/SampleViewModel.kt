package com.pexip.sdk.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.ui.renderWorkflowIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
    handle: SavedStateHandle,
    sampleWorkflow: SampleWorkflow
) : ViewModel() {

    private val _output = MutableSharedFlow<SampleOutput>(extraBufferCapacity = 1)

    val output = _output.asSharedFlow()
    val rendering by lazy {
        renderWorkflowIn(
            workflow = sampleWorkflow,
            scope = viewModelScope,
            prop = SampleProps("Pexip Video SDK"),
            savedStateHandle = handle,
            onOutput = _output::emit
        )
    }
}
