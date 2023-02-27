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

/**
 * Thrown to indicate that the [msg] was not sent.
 *
 * @property msg an instance of [Message] that was not sent
 * @property cause a cause of this exception
 */
public class MessageNotSentException @JvmOverloads constructor(
    public val msg: Message,
    cause: Throwable? = null,
) : RuntimeException("Failed to sent the message.", cause)
