/*
 * Copyright 2022-2025 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.sample

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.workflow1.android.renderWorkflowIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(
    handle: SavedStateHandle,
    sampleWorkflow: SampleWorkflow,
) : ViewModel() {

    private val _output = MutableSharedFlow<SampleOutput>(extraBufferCapacity = 1)

    val output = _output.asSharedFlow()
    val rendering by lazy {
        renderWorkflowIn(
            workflow = sampleWorkflow,
            scope = viewModelScope,
            savedStateHandle = handle,
            onOutput = _output::emit,
        )
    }
}
