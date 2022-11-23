package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.Bitrate
import com.pexip.sdk.media.Bitrate.Companion.bps
import org.webrtc.SessionDescription

internal fun SessionDescription.mangle(
    bitrate: Bitrate,
    mainAudioMid: String?,
    mainVideoMid: String?,
    presentationVideoMid: String?,
): SessionDescription {
    require(type == SessionDescription.Type.OFFER) { "type must be OFFER." }
    return SessionDescription(type) {
        val lines = splitToLineSequence()
        val mainAudioMidLine = mainAudioMid?.toMidLine()
        val mainVideoMidLine = mainVideoMid?.toMidLine()
        val presentationVideoMidLine = presentationVideoMid?.toMidLine()
        var section = Section.SESSION
        for (line in lines) {
            section = when {
                line.startsWith("m=audio") -> Section.AUDIO
                line.startsWith("m=video") -> Section.VIDEO
                else -> section
            }
            appendSdpLine(line)
            if (section == Section.VIDEO && line.startsWith("c=IN") && bitrate > 0.bps) {
                appendSdpLine(bitrate)
            }
            when (line) {
                mainAudioMidLine, mainVideoMidLine -> appendSdpLine("a=content:main")
                presentationVideoMidLine -> appendSdpLine("a=content:slides")
            }
        }
    }
}

internal fun SessionDescription.mangle(bitrate: Bitrate): SessionDescription {
    require(type == SessionDescription.Type.ANSWER) { "type must be ANSWER." }
    return SessionDescription(type) {
        val lines = splitToLineSequence()
        for (line in lines) {
            val result = TIAS.matchEntire(line)
            if (result == null) {
                appendSdpLine(line)
            } else {
                val sdpBitrate = result.groupValues[1].toIntOrNull()
                if (sdpBitrate == null || bitrate.bps > sdpBitrate || bitrate.bps == 0) {
                    appendSdpLine(line)
                } else {
                    appendSdpLine(bitrate)
                }
            }
        }
    }
}

private inline fun SessionDescription(
    type: SessionDescription.Type,
    block: StringBuilder.() -> Unit,
) = SessionDescription(type, buildString(block))

private fun SessionDescription.splitToLineSequence() =
    description.splitToSequence(DELIMITER).filter { it.isNotBlank() }

private fun String.toMidLine(): String = "a=mid:$this"

private fun StringBuilder.appendSdpLine(line: String) {
    append(line)
    append(DELIMITER)
}

private fun StringBuilder.appendSdpLine(bitrate: Bitrate) {
    appendSdpLine("b=TIAS:${bitrate.bps}")
}

private const val DELIMITER = "\r\n"

private enum class Section {
    SESSION, AUDIO, VIDEO
}

private val TIAS = Regex("^b=TIAS:(\\d+)$")
