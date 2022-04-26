package com.pexip.sdk.media

public interface MediaConnectionFactory {

    public fun createMediaConnection(config: MediaConnectionConfig): MediaConnection

    public fun dispose()
}
