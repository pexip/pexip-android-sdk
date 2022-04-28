package com.pexip.sdk.media.webrtc

import android.content.Context
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.ContextUtils
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import java.util.UUID
import java.util.concurrent.Executors

public class WebRtcMediaConnectionFactory(context: Context) : MediaConnectionFactory {

    public constructor() : this(ContextUtils.getApplicationContext())

    private val eglBase = EglBase.create()
    internal val factory = PeerConnectionFactory.builder()
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
        .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, false, false))
        .createPeerConnectionFactory()
    internal val applicationContext = context.applicationContext
    internal val cameraEnumerator = when (Camera2Enumerator.isSupported(applicationContext)) {
        true -> Camera2Enumerator(applicationContext)
        else -> Camera1Enumerator()
    }

    public val eglBaseContext: EglBase.Context
        get() = eglBase.eglBaseContext

    override fun createLocalAudioTrack(): LocalAudioTrack {
        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrackId = UUID.randomUUID().toString()
        val audioTrack = factory.createAudioTrack(audioTrackId, audioSource)
        return WebRtcLocalAudioTrack(audioSource, audioTrack)
    }

    override fun createMediaConnection(config: MediaConnectionConfig): WebRtcMediaConnection =
        WebRtcMediaConnection(
            factory = this,
            config = config,
            workerExecutor = Executors.newSingleThreadExecutor(),
            signalingExecutor = Executors.newSingleThreadExecutor()
        )

    override fun dispose() {
        factory.dispose()
        eglBase.release()
    }

    public companion object {

        @JvmStatic
        public fun initialize(context: Context) {
            val applicationContext = context.applicationContext
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
        }
    }
}
