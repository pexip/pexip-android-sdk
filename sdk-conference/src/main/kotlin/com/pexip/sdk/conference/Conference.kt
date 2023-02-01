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
package com.pexip.sdk.conference

import com.pexip.sdk.media.MediaConnectionSignaling

/**
 * Represents a conference.
 *
 * @property name a display name of this [Conference]
 */
public interface Conference : MediaConnectionSignaling {

    public val name: String

    /**
     * Registers a [ConferenceEventListener].
     *
     * @param listener a conference event listener
     */
    public fun registerConferenceEventListener(listener: ConferenceEventListener)

    /**
     * Unregisters a [ConferenceEventListener].
     *
     * @param listener a conference event listener
     */
    public fun unregisterConferenceEventListener(listener: ConferenceEventListener)

    /**
     * Sends DTMF digits to this [Conference].
     *
     * @param digits a sequence of valid DTMF digits
     */
    @Deprecated(
        message = "Use MediaConnection.dtmf() instead.",
        level = DeprecationLevel.ERROR,
    )
    public fun dtmf(digits: String)

    /**
     * Sends a plain text message to this [Conference].
     *
     * @param payload a plain text message
     */
    public fun message(payload: String)

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()
}
