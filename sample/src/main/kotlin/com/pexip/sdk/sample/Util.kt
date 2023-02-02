/*
 * Copyright 2022 Pexip AS
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
import androidx.core.content.ContextCompat
import com.squareup.workflow1.BaseRenderContext
import com.squareup.workflow1.WorkflowAction

fun <Props, State, Output> BaseRenderContext<Props, State, Output>.send(action: () -> WorkflowAction<Props, State, Output>): () -> Unit =
    { actionSink.send(action()) }

fun <Props, State, Output, T> BaseRenderContext<Props, State, Output>.send(action: (T) -> WorkflowAction<Props, State, Output>): (T) -> Unit =
    { actionSink.send(action(it)) }

inline fun log(tag: String, priority: Int = Log.INFO, block: () -> String) {
    if (Log.isLoggable(tag, priority)) Log.println(priority, tag, block())
}

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
