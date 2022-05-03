package com.pexip.sdk.media

import androidx.annotation.MainThread

public interface LocalVideoTrack : VideoTrack {

    public fun startCapture(profile: QualityProfile)

    public fun stopCapture()

    public fun registerCapturingListener(listener: CapturingListener)

    public fun unregisterCapturingListener(listener: CapturingListener)

    public fun dispose()

    public fun interface CapturingListener {

        @MainThread
        public fun onCapturing(capturing: Boolean)
    }
}
