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
package com.pexip.sdk.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A service type.
 */
@Serializable
public enum class ServiceType {

    /**
     * If a dial-out participant that has not answered.
     */
    @SerialName("connecting")
    CONNECTING,

    /**
     * If waiting to be allowed to join a locked conference.
     */
    @SerialName("waiting_room")
    WAITING_ROOM,

    /**
     * If on the PIN entry screen.
     */
    @SerialName("ivr")
    IVR,

    /**
     * If in a VMR.
     */
    @SerialName("conference")
    CONFERENCE,

    /**
     * If in a Virtual Auditorium.
     */
    @SerialName("lecture")
    LECTURE,

    /**
     * If in a gateway call.
     */
    @SerialName("gateway")
    GATEWAY,

    /**
     * If in a test call.
     */
    @SerialName("test_call")
    TEST_CALL,

    /**
     * An unknown service type.
     *
     * This is provided for forward-compatibility; if you encounter this value consider updating the
     * version of the SDK.
     */
    UNKNOWN,
}
