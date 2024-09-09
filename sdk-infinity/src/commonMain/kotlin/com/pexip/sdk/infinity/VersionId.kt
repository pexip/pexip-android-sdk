/*
 * Copyright 2024 Pexip AS
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

import kotlinx.serialization.Serializable

/**
 * A unique identifier for the Infinity version.
 *
 * @property value a unique identifier for the Infinity version.
 */
@Serializable
@JvmInline
public value class VersionId(public val value: String) : Comparable<VersionId> {

    override fun compareTo(other: VersionId): Int = value.compareTo(other.value)

    override fun toString(): String = value

    public companion object {

        public val V29: VersionId = VersionId("29")
        public val V35: VersionId = VersionId("35")
        public val V35_1: VersionId = VersionId("35.1")

        /**
         * New APIs:
         * - client_mute
         * - client_unmute
         * - is_client_muted
         */
        public val V36: VersionId = VersionId("36")
    }
}
