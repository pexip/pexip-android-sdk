package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

internal class WebRtcLocalAudioTrack(
    private val audioHandler: AudioHandler,
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
    private val workerExecutor: Executor,
    private val signalingExecutor: Executor,
) : LocalAudioTrack {

    private val disposed = AtomicBoolean()
    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()

    @Volatile
    private var capturing = audioTrack.enabled()

    init {
        workerExecutor.maybeExecute(audioHandler::start)
    }

    override fun startCapture() {
        if (capturing) return
        workerExecutor.maybeExecute {
            if (audioTrack.setEnabled(true)) {
                capturing = true
                signalingExecutor.maybeExecute {
                    capturingListeners.forEach {
                        it.safeOnCapturing(true)
                    }
                }
            }
        }
    }

    override fun stopCapture() {
        if (!capturing) return
        workerExecutor.maybeExecute {
            if (audioTrack.setEnabled(false)) {
                capturing = false
                signalingExecutor.maybeExecute {
                    capturingListeners.forEach {
                        it.safeOnCapturing(false)
                    }
                }
            }
        }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        signalingExecutor.maybeExecute {
            listener.safeOnCapturing(capturing)
        }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                audioHandler.stop()
                audioTrack.dispose()
                audioSource.dispose()
            }
        }
    }
}
