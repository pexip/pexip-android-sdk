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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.BreakoutBeginEvent
import com.pexip.sdk.api.infinity.BreakoutEndEvent
import com.pexip.sdk.conference.Breakout
import com.pexip.sdk.conference.Breakouts
import com.pexip.sdk.infinity.BreakoutId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn

internal class BreakoutsImpl(scope: CoroutineScope, event: Flow<Event>) : Breakouts {

    override val breakouts: StateFlow<Map<BreakoutId, Breakout>> = channelFlow {
        val map = mutableMapOf<BreakoutId, Breakout>()
        event.collect {
            when (it) {
                is BreakoutBeginEvent -> {
                    map[it.id] = BreakoutImpl(it.id, it.participantId)
                    send(map.toMap())
                }
                is BreakoutEndEvent -> {
                    map -= it.id
                    send(map.toMap())
                }
                else -> Unit
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())
}
