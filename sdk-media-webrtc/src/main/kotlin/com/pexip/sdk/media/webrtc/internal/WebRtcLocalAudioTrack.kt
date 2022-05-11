package com.pexip.sdk.media.webrtc.internal

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet

internal class WebRtcLocalAudioTrack(
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
) : LocalAudioTrack {

    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()

    @Volatile
    private var capturing = audioTrack.enabled()

    override fun startCapture() {
        if (capturing) return
        handler.post {
            if (audioTrack.setEnabled(true)) {
                capturing = true
                capturingListeners.forEach { it.onCapturing(true) }
            }
        }
    }

    override fun stopCapture() {
        if (!capturing) return
        handler.post {
            if (audioTrack.setEnabled(false)) {
                capturing = false
                capturingListeners.forEach { it.onCapturing(false) }
            }
        }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        handler.post { listener.onCapturing(capturing) }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        audioTrack.dispose()
        audioSource.dispose()
        handler.removeCallbacksAndMessages(null)
    }
}
