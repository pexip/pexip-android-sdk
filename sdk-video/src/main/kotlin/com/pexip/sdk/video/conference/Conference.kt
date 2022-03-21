package com.pexip.sdk.video.conference

import com.pexip.sdk.video.conference.internal.RealConference
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.token.Token
import okhttp3.OkHttpClient

/**
 * Represents a conference.
 */
public interface Conference {

    public val callHandler: CallHandler

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()

    public companion object {

        @JvmStatic
        public fun create(token: Token, client: OkHttpClient = OkHttpClient): Conference =
            RealConference(client, token)
    }
}
