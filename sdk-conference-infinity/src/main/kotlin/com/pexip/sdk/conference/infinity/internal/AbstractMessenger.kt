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

import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageListener
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.SendCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

internal abstract class AbstractMessenger(private val scope: CoroutineScope) : Messenger {

    private val listeners = CopyOnWriteArraySet<MessageListener>()

    @Deprecated("Use the coroutines version of this method.")
    override fun send(type: String, payload: String, callback: SendCallback) =
        sendInternal(type, payload, callback)

    @Deprecated("Use the coroutines version of this method.")
    override fun send(participantId: UUID, type: String, payload: String, callback: SendCallback) =
        sendInternal(type, payload, callback, participantId)

    @Deprecated("Use getMessages() that returns a Flow")
    override fun registerMessageListener(listener: MessageListener) {
        listeners += listener
    }

    @Deprecated("Use getMessages() that returns a Flow")
    override fun unregisterMessageListener(listener: MessageListener) {
        listeners -= listener
    }

    protected fun onMessage(message: Message) = listeners.forEach { it.onMessage(message) }

    private fun sendInternal(
        type: String,
        payload: String,
        callback: SendCallback,
        participantId: UUID? = null,
    ) {
        scope.launch {
            try {
                val message = send(type, payload, participantId)
                callback.onSuccess(message)
            } catch (e: MessageNotSentException) {
                callback.onFailure(e)
            }
        }
    }
}
