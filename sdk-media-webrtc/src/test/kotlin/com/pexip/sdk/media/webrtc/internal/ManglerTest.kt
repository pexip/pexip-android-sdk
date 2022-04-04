package com.pexip.sdk.media.webrtc.internal

import okio.FileSystem
import okio.Path.Companion.toPath
import org.webrtc.SessionDescription
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ManglerTest {

    @Test
    fun `mangles SessionDescription`() {
        // Both files MUST be CRLF
        val originalSdp = SessionDescription(
            SessionDescription.Type.OFFER,
            read("session_description_original")
        )
        val mangledSdp = originalSdp.mangle(
            mainAudioMid = "3",
            mainVideoMid = "4",
            presentationVideoMid = "5"
        )
        assertEquals(originalSdp.type, mangledSdp.type)
        assertEquals(
            expected = read("session_description_mangled"),
            actual = mangledSdp.description
        )
    }

    private fun read(fileName: String) = FileSystem.RESOURCES.read(fileName.toPath()) { readUtf8() }
}
