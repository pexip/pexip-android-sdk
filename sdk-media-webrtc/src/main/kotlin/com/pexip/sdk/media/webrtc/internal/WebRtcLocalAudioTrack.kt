package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

internal class WebRtcLocalAudioTrack(
    context: Context,
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
    private val workerExecutor: Executor,
    private val signalingExecutor: Executor,
) : LocalAudioTrack {

    private val disposed = AtomicBoolean()
    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()
    private val microphoneMuteObserver = MicrophoneMuteObserver(context) { microphoneMute ->
        signalingExecutor.maybeExecute {
            capturingListeners.forEach {
                it.safeOnCapturing(!microphoneMute)
            }
        }
    }

    override fun startCapture() {
        workerExecutor.maybeExecute {
            microphoneMuteObserver.microphoneMute = false
        }
    }

    override fun stopCapture() {
        workerExecutor.maybeExecute {
            microphoneMuteObserver.microphoneMute = true
        }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        signalingExecutor.maybeExecute {
            listener.safeOnCapturing(!microphoneMuteObserver.microphoneMute)
        }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                microphoneMuteObserver.dispose()
                audioTrack.dispose()
                audioSource.dispose()
            }
        }
    }
}
