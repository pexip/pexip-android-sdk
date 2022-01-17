package com.pexip.sdk.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.ui.renderWorkflowIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ConferenceViewModel(handle: SavedStateHandle) : ViewModel() {

    private val _output = MutableSharedFlow<ConferenceOutput>(extraBufferCapacity = 1)

    val output = _output.asSharedFlow()
    val rendering by lazy {
        renderWorkflowIn(
            workflow = ConferenceWorkflow(),
            scope = viewModelScope,
            prop = handle.get<ConferenceProps>(ConferenceActivity.KEY_CONFERENCE_PROPS)!!,
            savedStateHandle = handle,
            onOutput = _output::emit
        )
    }
}
