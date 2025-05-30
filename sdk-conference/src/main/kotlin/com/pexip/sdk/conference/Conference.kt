/*
 * Copyright 2022-2024 Pexip AS
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

import com.pexip.sdk.core.ExperimentalSdkApi
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.VersionId
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionSignaling

/**
 * Represents a conference.
 *
 * @property versionId a version ID of this [Conference]
 * @property name a display name of this [Conference]
 * @property theme an instance of [Theme] attached to this [Conference]
 * @property roster an instance of [Roster] attached to this [Conference]
 * @property referer an instance of [Referer] attached to this [Conference]
 * @property messenger an instance of [Messenger] attached to this [Conference]
 * @property breakouts an instance of [Breakouts] attached to this [Conference]
 * @property signaling an instance of [MediaConnectionSignaling] to be used with [MediaConnection]
 */
public interface Conference {

    public val versionId: VersionId
        get() = throw NotImplementedError()

    public val name: String

    public val theme: Theme

    public val roster: Roster

    public val referer: Referer

    public val messenger: Messenger

    @ExperimentalSdkApi
    public val breakouts: Breakouts get() = throw NotImplementedError()

    public val serviceType: ServiceType

    public val signaling: MediaConnectionSignaling

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
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()
}
