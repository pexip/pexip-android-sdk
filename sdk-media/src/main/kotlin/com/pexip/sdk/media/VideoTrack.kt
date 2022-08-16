package com.pexip.sdk.media

/**
 * A video track that may be rendered onto a [Renderer].
 */
public interface VideoTrack {

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
