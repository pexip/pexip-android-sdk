package com.pexip.sdk.media

import android.view.SurfaceView

public interface VideoTrack {

    public fun addRenderer(renderer: SurfaceView)

    public fun removeRenderer(renderer: SurfaceView)
}
