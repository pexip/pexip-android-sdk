package com.pexip.sdk.video.conference.internal

import android.content.Context
import androidx.startup.Initializer
import org.webrtc.PeerConnectionFactory

internal class WebRtcInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
