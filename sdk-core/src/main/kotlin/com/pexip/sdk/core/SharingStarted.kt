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
package com.pexip.sdk.core

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Sharing is started when the first subscriber appears after a given [timeout] has passed since the
 * most recent subscription and stops if there are no subscribers.
 */
@Suppress("FunctionName")
@InternalSdkApi
public fun SharingStarted.Companion.WhileSubscribedWithDebounce(timeout: Duration = 100.milliseconds): SharingStarted =
    StartedWhileSubscribedWithDebounce(timeout)

@OptIn(FlowPreview::class)
private class StartedWhileSubscribedWithDebounce(private val timeout: Duration) : SharingStarted {

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        subscriptionCount
            .map { if (it > 0) SharingCommand.START else SharingCommand.STOP_AND_RESET_REPLAY_CACHE }
            .debounce { if (it == SharingCommand.START) timeout else Duration.ZERO }
            .distinctUntilChanged()

    override fun toString(): String = "SharingStarted.WhileSubscribedWithDebounce($timeout)"
}
