/*
 * Copyright 2022 Pexip AS
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
package com.pexip.sdk.media

/**
 * The list of possible bitrate measurement units, in which a bitrate can be expressed.
 *
 * The smallest time unit is [BPS] and the largest is [MBPS], which corresponds to exactly 1000000 [BPS].
 */
public enum class BitrateUnit {

    /**
     * Bitrate unit representing one bit per second.
     */
    BPS,

    /**
     * Bitrate unit representing one kilobit per second, which is equal to 1000 bits per second.
     */
    KBPS,

    /**
     * Bitrate unit representing one megabit per second, which is equal to 1000 kilobits per second.
     */
    MBPS,
}
