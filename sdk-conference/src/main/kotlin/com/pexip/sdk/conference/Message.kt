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
package com.pexip.sdk.conference

import java.util.UUID

/**
 * A message that can be received from [Messenger].
 *
 * @property at a timestamp in milliseconds
 * @property participantId a unique identifier of the sender
 * @property participantName a display name of the sender
 * @property type a payload's MIME type (e.g. "text/plain")
 * @property payload actual contents of this [Message]
 * @property direct true if this is a direct message, false otherwise
 */
public data class Message(
    val at: Long,
    val participantId: UUID,
    val participantName: String,
    val type: String,
    val payload: String,
    val direct: Boolean,
)
