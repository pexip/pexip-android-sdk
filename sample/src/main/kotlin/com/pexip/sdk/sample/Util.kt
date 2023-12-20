/*
 * Copyright 2022-2023 Pexip AS
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

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.ContextCompat
import com.squareup.workflow1.BaseRenderContext
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.ui.TextController
import kotlinx.coroutines.launch

fun <Props, State, Output> BaseRenderContext<Props, State, Output>.send(action: () -> WorkflowAction<Props, State, Output>): () -> Unit =
    { actionSink.send(action()) }

fun <Props, State, Output, T> BaseRenderContext<Props, State, Output>.send(action: (T) -> WorkflowAction<Props, State, Output>): (T) -> Unit =
    { actionSink.send(action(it)) }

inline fun log(tag: String, priority: Int = Log.INFO, block: () -> String) {
    if (Log.isLoggable(tag, priority)) Log.println(priority, tag, block())
}

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
fun TextController.asMutableState(): MutableState<TextFieldValue> {
    val state = remember(this) {
        val value = TextFieldValue(textValue, TextRange(textValue.length))
        mutableStateOf(value)
    }
    LaunchedEffect(this) {
        launch { onTextChanged.collect { state.value = state.value.copy(text = it) } }
        snapshotFlow { state.value }.collect { textValue = it.text }
    }
    return state
}
