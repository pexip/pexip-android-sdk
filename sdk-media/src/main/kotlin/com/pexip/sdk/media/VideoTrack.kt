package com.pexip.sdk.media

public interface VideoTrack {

    @Deprecated(
        message = "Use addRenderer that accepts a Renderer.",
        level = DeprecationLevel.ERROR
    )
    public fun addRenderer(renderer: Any)

    @Deprecated(
        message = "Use removeRenderer that accepts a Renderer.",
        level = DeprecationLevel.ERROR
    )
    public fun removeRenderer(renderer: Any)

    public fun addRenderer(renderer: Renderer)

    public fun removeRenderer(renderer: Renderer)
}
