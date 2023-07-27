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

import com.pexip.sdk.media.Bitrate
import com.pexip.sdk.media.Bitrate.Companion.bps
import org.webrtc.SessionDescription

internal fun SessionDescription.mangle(
    bitrate: Bitrate,
    mainAudioMid: String?,
    mainVideoMid: String?,
    presentationVideoMid: String?,
): SessionDescription = SessionDescription(type) {
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

internal fun SessionDescription.mangle(bitrate: Bitrate) = SessionDescription(type) {
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

internal fun SessionDescription.splitToLineSequence() =
    description.splitToSequence(DELIMITER).filter { it.isNotBlank() }

private inline fun SessionDescription(
    type: SessionDescription.Type,
    block: StringBuilder.() -> Unit,
) = SessionDescription(type, buildString(block))

private fun String.toMidLine(): String = "a=mid:$this"

private fun StringBuilder.appendSdpLine(line: String) {
    append(line)
    append(DELIMITER)
}

private fun StringBuilder.appendSdpLine(bitrate: Bitrate) {
    append("b=TIAS:")
    appendSdpLine(bitrate.bps.toString())
}

private const val DELIMITER = "\r\n"

private enum class Section {
    SESSION, AUDIO, VIDEO
}

private val TIAS = Regex("^b=TIAS:(\\d+)$")
