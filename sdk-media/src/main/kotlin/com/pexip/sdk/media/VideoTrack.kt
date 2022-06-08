package com.pexip.sdk.media

public interface VideoTrack {

    @Deprecated("Use addRenderer that accepts a Renderer.")
    public fun addRenderer(renderer: Any)

    @Deprecated("Use removeRenderer that accepts a Renderer.")
    public fun removeRenderer(renderer: Any)

    public fun addRenderer(renderer: Renderer)

    public fun removeRenderer(renderer: Renderer)
}
