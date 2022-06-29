package com.pexip.sdk.media

/**
 * A video track that may be rendered onto a [Renderer].
 */
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

    /**
     * Adds the supplied [renderer] and starts drawing on it.
     *
     * @param renderer a renderer to add
     */
    public fun addRenderer(renderer: Renderer)

    /**
     * Removes the supplied [renderer] and stops drawing on it.
     *
     * @param renderer a renderer to remove
     */
    public fun removeRenderer(renderer: Renderer)
}
