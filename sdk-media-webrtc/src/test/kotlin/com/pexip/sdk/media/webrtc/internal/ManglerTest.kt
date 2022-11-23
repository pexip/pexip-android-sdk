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
        val mangled = original.mangle(
            bitrate = 576.kbps,
            mainAudioMid = "3",
            mainVideoMid = "4",
            presentationVideoMid = "5"
        )
        assertEquals(original.type, mangled.type)
        assertEquals(read("offer_mangled"), mangled.description)
    }

    @Test
    fun `mangles offer, bitrate is zero`() {
        val original = SessionDescription(SessionDescription.Type.OFFER, read("offer_original"))
        val mangled = original.mangle(
            bitrate = 0.bps,
            mainAudioMid = "3",
            mainVideoMid = "4",
            presentationVideoMid = "5"
        )
        assertEquals(original.type, mangled.type)
        assertEquals(read("offer_mangled_bitrate_zero"), mangled.description)
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
