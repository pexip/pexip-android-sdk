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
    private val microphoneMuteListener = AudioHandler.MicrophoneMuteListener { microphoneMute ->
        signalingExecutor.maybeExecute {
            capturingListeners.forEach {
                it.safeOnCapturing(!microphoneMute)
            }
        }
    }

    init {
        workerExecutor.maybeExecute {
            audioHandler.registerMicrophoneMuteListener(microphoneMuteListener)
            audioHandler.start()
        }
    }

    override fun startCapture() {
        workerExecutor.maybeExecute {
            audioHandler.microphoneMute = false
        }
    }

    override fun stopCapture() {
        workerExecutor.maybeExecute {
            audioHandler.microphoneMute = true
        }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        signalingExecutor.maybeExecute {
            listener.safeOnCapturing(!audioHandler.microphoneMute)
        }
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            workerExecutor.execute {
                audioHandler.unregisterMicrophoneMuteListener(microphoneMuteListener)
                audioHandler.stop()
                audioTrack.dispose()
                audioSource.dispose()
            }
        }
    }
}
