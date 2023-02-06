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

import com.pexip.sdk.media.Bitrate.Companion.bps
import com.pexip.sdk.media.Bitrate.Companion.kbps
import com.pexip.sdk.media.Bitrate.Companion.mbps
import com.pexip.sdk.media.Bitrate.Companion.toBitrate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class BitrateTest {

    @Test
    fun `toBitrate throws if the value is negative`() {
        val value = -Random.nextInt(1, Int.MAX_VALUE)
        assertFailsWith<IllegalArgumentException> { value.toBitrate(BitrateUnit.BPS) }
    }

    @Test
    fun `toBitrate throws if the value is too high`() {
        assertFailsWith<IllegalArgumentException> { Int.MAX_VALUE.toBitrate(BitrateUnit.MBPS) }
    }

    @Test
    fun `bps produces correct Bitrate`() {
        val value = Random.nextInt(1, Int.MAX_VALUE)
        val bitrate = value.bps
        assertEquals(value, bitrate.bps)
    }

    @Test
    fun `kbps produces correct Bitrate`() {
        val value = Random.nextInt(1, Int.MAX_VALUE / 1_000)
        val bitrate = value.kbps
        assertEquals(value * 1_000, bitrate.bps)
    }

    @Test
    fun `mbps produces correct Bitrate`() {
        val value = Random.nextInt(1, Int.MAX_VALUE / 1_000_000)
        val bitrate = value.mbps
        assertEquals(value * 1_000_000, bitrate.bps)
    }

    @Test
    fun `Bitrate can be compared`() {
        assertTrue { 1000.bps == 1.kbps }
        assertTrue { 100.bps > 1.bps }
        assertTrue { 100.bps < 1.kbps }
        assertTrue { 1.mbps > 100.kbps }
    }
}
