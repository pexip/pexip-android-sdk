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
package com.pexip.sdk.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A conference role that affects privileges that a participant has
 *
 * See [documentation](https://docs.pexip.com/admin/pins_hosts_guests.htm) for additional info.
 */
@Serializable
public enum class Role {

    /**
     * Has the ability to control the conference.
     */
    @SerialName("chair")
    HOST,

    /**
     * Can only participate in the conference.
     */
    @SerialName("guest")
    GUEST,

    /**
     * An unknown role.
     *
     * This is provided for forward-compatibility; if you encounter this value consider updating the
     * version of the SDK.
     */
    UNKNOWN,
}
