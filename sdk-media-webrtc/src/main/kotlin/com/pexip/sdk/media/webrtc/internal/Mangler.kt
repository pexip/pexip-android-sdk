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
): MangleResult {
    require(type == SessionDescription.Type.OFFER) { "type must be OFFER." }
    val builder = StringBuilder()
    val lines = splitToLineSequence()
    val mainAudioMidLine = mainAudioMid?.toMidLine()
    val mainVideoMidLine = mainVideoMid?.toMidLine()
    val presentationVideoMidLine = presentationVideoMid?.toMidLine()
    var section = Section.SESSION
    var ufrag: String? = null
    var pwd: String? = null
    var mid: String? = null
    val iceCredentials = mutableMapOf<String, IceCredentials>()
    for (line in lines) {
        section = when {
            line.startsWith("m=audio") -> Section.AUDIO
            line.startsWith("m=video") -> Section.VIDEO
            else -> section
        }
        builder.appendSdpLine(line)
        if (section == Section.VIDEO && line.startsWith("c=IN") && bitrate > 0.bps) {
            builder.appendSdpLine(bitrate)
        }
        when (line) {
            mainAudioMidLine, mainVideoMidLine -> builder.appendSdpLine("a=content:main")
            presentationVideoMidLine -> builder.appendSdpLine("a=content:slides")
        }
        if (line.startsWith(ICE_UFRAG)) ufrag = line.removePrefix(ICE_UFRAG)
        if (line.startsWith(ICE_PWD)) pwd = line.removePrefix(ICE_PWD)
        if (line.startsWith(MID)) mid = line.removePrefix(MID)
        if (ufrag != null && pwd != null && mid != null) {
            iceCredentials[mid] = IceCredentials(ufrag, pwd)
            ufrag = null
            pwd = null
            mid = null
        }
    }
    return MangleResult(
        description = SessionDescription(type, builder.toString()),
        iceCredentials = iceCredentials.toMap(),
    )
}

internal fun SessionDescription.mangle(bitrate: Bitrate): SessionDescription {
    require(type == SessionDescription.Type.ANSWER) { "type must be ANSWER." }
    val newDescription = buildString {
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
    return SessionDescription(type, newDescription)
}

internal data class MangleResult(
    val description: SessionDescription,
    val iceCredentials: Map<String, IceCredentials>,
)

internal data class IceCredentials(val ufrag: String, val pwd: String)

private fun SessionDescription.splitToLineSequence() =
    description.splitToSequence(DELIMITER).filter { it.isNotBlank() }

private fun String.toMidLine(): String = MID + this

private fun StringBuilder.appendSdpLine(line: String) {
    append(line)
    append(DELIMITER)
}

private fun StringBuilder.appendSdpLine(bitrate: Bitrate) {
    append("b=TIAS:")
    appendSdpLine(bitrate.bps.toString())
}

private const val DELIMITER = "\r\n"
private const val MID = "a=mid:"
private const val ICE_UFRAG = "a=ice-ufrag:"
private const val ICE_PWD = "a=ice-pwd:"

private enum class Section {
    SESSION, AUDIO, VIDEO
}

private val TIAS = Regex("^b=TIAS:(\\d+)$")
