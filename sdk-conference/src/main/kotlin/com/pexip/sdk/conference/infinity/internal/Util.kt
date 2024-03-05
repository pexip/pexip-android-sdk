/*
 * Copyright 2024 Pexip AS
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
package com.pexip.sdk.conference.infinity.internal

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

@OptIn(FlowPreview::class)
@Suppress("ktlint:standard:function-naming")
internal fun SharingStarted.Companion.WhileSubscribedWithDebounce(timeout: Duration) =
    SharingStarted { subscriptionCount ->
        subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .debounce(timeout)
            .map { if (it) SharingCommand.START else SharingCommand.STOP_AND_RESET_REPLAY_CACHE }
    }
