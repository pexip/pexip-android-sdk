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
package com.pexip.sdk.sample.chat

import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWorkflow @Inject constructor() :
    StatefulWorkflow<ChatProps, ChatState, ChatOutput, ChatRendering>() {

    override fun initialState(props: ChatProps, snapshot: Snapshot?): ChatState =
        ChatState(messageWorker = MessageWorker(props.messenger))

    override fun snapshotState(state: ChatState): Snapshot? = null

    override fun onPropsChanged(old: ChatProps, new: ChatProps, state: ChatState): ChatState =
        if (old == new) state else initialState(new, null)

    override fun render(
        renderProps: ChatProps,
        renderState: ChatState,
        context: RenderContext,
    ): ChatRendering {
        context.runningWorker(
            worker = renderState.messageWorker,
            handler = ::onMessageWorkerOutput,
        )
        context.runningWorker(
            worker = renderState.blankPayloadWorker,
            handler = ::onBlankPayloadWorkerOutput,
        )
        if (renderState.sendMessageWorker != null) {
            context.runningWorker(
                worker = renderState.sendMessageWorker,
                handler = ::onSendMessageWorkerOutput,
            )
        }
        return ChatRendering(
            payload = renderState.payload,
            messages = renderState.messages,
            submitEnabled = !renderState.blankPayload,
            onSubmitClick = context.send(::onSubmitClick),
            onBackClick = context.send(::onBackClick),
        )
    }

    private fun onMessageWorkerOutput(message: Message) =
        action({ "onMessageWorkerOutput($message)" }) {
            state = state.copy(messages = state.messages + message)
        }

    private fun onBlankPayloadWorkerOutput(value: Boolean) =
        action({ "onBlankPayloadWorkerOutput($value)" }) {
            state = state.copy(blankPayload = value)
        }

    private fun onSendMessageWorkerOutput(message: Message?) =
        action({ "onSendMessageWorkerOutput($message)" }) {
            if (message != null) {
                state.payload.textValue = ""
            }
            state = state.copy(
                messages = when (message) {
                    null -> state.messages
                    else -> state.messages + message
                },
                sendMessageWorker = null,
            )
        }

    private fun onSubmitClick() = action({ "onSubmitClick()" }) {
        val worker = SendMessageWorker(
            messenger = props.messenger,
            payload = state.payload.textValue,
        )
        state = state.copy(sendMessageWorker = worker)
    }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(ChatOutput.Back)
    }

    private class MessageWorker(private val messenger: Messenger) : Worker<Message> {

        override fun run(): Flow<Message> = messenger.message

        override fun doesSameWorkAs(otherWorker: Worker<*>): Boolean =
            otherWorker is MessageWorker && messenger == otherWorker.messenger
    }

    private class SendMessageWorker(
        private val messenger: Messenger,
        private val payload: String,
    ) : Worker<Message?> {

        override fun run(): Flow<Message?> = flow {
            val message = try {
                when (val payload = payload.trim()) {
                    "" -> null
                    else -> messenger.send(type = "text/plain", payload = payload)
                }
            } catch (e: MessageNotSentException) {
                null
            }
            emit(message)
        }

        override fun doesSameWorkAs(otherWorker: Worker<*>): Boolean =
            otherWorker is SendMessageWorker && this.messenger == otherWorker.messenger && this.payload == otherWorker.payload
    }
}
