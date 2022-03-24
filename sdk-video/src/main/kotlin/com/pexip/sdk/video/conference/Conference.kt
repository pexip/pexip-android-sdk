package com.pexip.sdk.video.conference

import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.RequestTokenResponse
import com.pexip.sdk.video.conference.internal.RealConference

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
        public fun create(
            service: InfinityService,
            node: Node,
            conferenceAlias: ConferenceAlias,
            response: RequestTokenResponse,
        ): Conference = RealConference(service, node, conferenceAlias, response)
    }
}
