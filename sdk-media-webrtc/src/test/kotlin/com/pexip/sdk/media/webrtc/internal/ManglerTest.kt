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
package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.Bitrate.Companion.bps
import com.pexip.sdk.media.Bitrate.Companion.kbps
import com.pexip.sdk.media.Bitrate.Companion.mbps
import okio.FileSystem
import okio.Path.Companion.toPath
import org.webrtc.SessionDescription
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ManglerTest {

    @Test
    fun `mangles offer`() {
        val original = SessionDescription(SessionDescription.Type.OFFER, read("offer_original"))
        val description = original.mangle(
            bitrate = 576.kbps,
            mainAudioMid = "3",
            mainVideoMid = "4",
            presentationVideoMid = "5",
        )
        assertEquals(original.type, description.type)
        assertEquals(read("offer_mangled"), description.description)
    }

    @Test
    fun `mangles offer, bitrate is zero`() {
        val original = SessionDescription(SessionDescription.Type.OFFER, read("offer_original"))
        val description = original.mangle(
            bitrate = 0.bps,
            mainAudioMid = "3",
            mainVideoMid = "4",
            presentationVideoMid = "5",
        )
        assertEquals(original.type, description.type)
        assertEquals(read("offer_mangled_bitrate_zero"), description.description)
    }

    @Test
    fun `mangles answer, bitrate is lower than original`() {
        val original = SessionDescription(SessionDescription.Type.ANSWER, read("answer_original"))
        val mangled = original.mangle(576.kbps)
        assertEquals(original.type, mangled.type)
        assertEquals(read("answer_mangled"), mangled.description)
    }

    @Test
    fun `mangles answer, bitrate is higher than original`() {
        val original = SessionDescription(SessionDescription.Type.ANSWER, read("answer_original"))
        val mangled = original.mangle(10.mbps)
        assertEquals(original.type, mangled.type)
        assertEquals(read("answer_original"), mangled.description)
    }

    @Test
    fun `mangles answer, bitrate is zero`() {
        val original = SessionDescription(SessionDescription.Type.ANSWER, read("answer_original"))
        val mangled = original.mangle(0.bps)
        assertEquals(original.type, mangled.type)
        assertEquals(read("answer_original"), mangled.description)
    }

    private fun read(fileName: String) = FileSystem.RESOURCES.read(fileName.toPath()) { readUtf8() }
}
