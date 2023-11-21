/*
 * Copyright 2023 Pexip AS
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

import com.pexip.sdk.media.Data
import com.pexip.sdk.media.DataChannel
import com.pexip.sdk.media.DataSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DataChannelImpl(override val id: Int) : DataChannel {

    private val mutex = Mutex()
    private val _data = MutableSharedFlow<Data>()

    private var sender: DataSender? = null

    override val data: Flow<Data> = _data.asSharedFlow()

    override suspend fun send(data: Data) = mutex.withLock {
        val sender = checkNotNull(sender) { "sender is not attached." }
        sender.send(data)
    }

    suspend fun attach(sender: DataSender) = mutex.withLock {
        this.sender = sender
    }

    suspend fun detach(sender: DataSender) = mutex.withLock {
        if (this.sender == sender) {
            this.sender = null
        }
    }

    suspend fun onData(data: Data) = _data.emit(data)
}
