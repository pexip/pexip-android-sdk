/*
 * Copyright 2023-2024 Pexip AS
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

import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

/**
 * A service type.
 */
public enum class ServiceType {

    /**
     * If a dial-out participant that has not answered.
     */
    CONNECTING,

    /**
     * If waiting to be allowed to join a locked conference.
     */
    WAITING_ROOM,

    /**
     * If on the PIN entry screen.
     */
    IVR,

    /**
     * If in a VMR.
     */
    CONFERENCE,

    /**
     * If in a Virtual Auditorium.
     */
    LECTURE,

    /**
     * If in a gateway call.
     */
    GATEWAY,

    /**
     * If in a test call.
     */
    TEST_CALL,

    /**
     * An unknown service type.
     *
     * This is provided for forward-compatibility; if you encounter this value consider updating the
     * version of the SDK.
     */
    UNKNOWN,
}

internal fun ApiServiceType.toServiceType() = when (this) {
    ApiServiceType.CONNECTING -> ServiceType.CONNECTING
    ApiServiceType.WAITING_ROOM -> ServiceType.WAITING_ROOM
    ApiServiceType.IVR -> ServiceType.IVR
    ApiServiceType.CONFERENCE -> ServiceType.CONFERENCE
    ApiServiceType.LECTURE -> ServiceType.LECTURE
    ApiServiceType.GATEWAY -> ServiceType.GATEWAY
    ApiServiceType.TEST_CALL -> ServiceType.TEST_CALL
    ApiServiceType.UNKNOWN -> ServiceType.UNKNOWN
}
