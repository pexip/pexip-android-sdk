package com.pexip.sdk.sample

import android.content.Context
import androidx.startup.Initializer
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory

class WebRtcMediaConnectionFactoryInitializer : Initializer<Unit> {

    override fun create(context: Context) = WebRtcMediaConnectionFactory.initialize(context)

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
