package com.pexip.sdk.media.webrtc.internal

import org.webrtc.SessionDescription

internal fun SessionDescription.mangle(
    mainAudioMid: String? = null,
    mainVideoMid: String? = null,
    presentationVideoMid: String? = null,
) = SessionDescription(
    type,
    buildString {
        val lines = description.splitToSequence(DELIMITER).filter { it.isNotBlank() }
        val mainAudioMidLine = mainAudioMid?.toMidLine()
        val mainVideoMidLine = mainVideoMid?.toMidLine()
        val presentationVideoMidLine = presentationVideoMid?.toMidLine()
        for (line in lines) {
            appendSdpLine(line)
            when (line) {
                mainAudioMidLine, mainVideoMidLine -> appendSdpLine("a=content:main")
                presentationVideoMidLine -> appendSdpLine("a=content:slides")
            }
        }
    }
)

private fun String.toMidLine(): String = "a=mid:$this"

private fun StringBuilder.appendSdpLine(line: String) {
    append(line)
    append(DELIMITER)
}

private const val DELIMITER = "\r\n"
