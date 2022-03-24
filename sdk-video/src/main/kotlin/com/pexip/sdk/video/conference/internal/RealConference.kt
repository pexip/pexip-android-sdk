package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.RequestTokenResponse
import com.pexip.sdk.video.conference.CallHandler
import com.pexip.sdk.video.conference.Conference

internal class RealConference(
    service: InfinityService,
    node: Node,
    conferenceAlias: ConferenceAlias,
    response: RequestTokenResponse,
) : Conference {

    private val store = TokenStore(response.token, response.expires)
    private val tokenHandler = TokenHandler(
        node = node,
        conferenceAlias = conferenceAlias,
        store = store,
        service = service
    )

    override val callHandler: CallHandler = CallHandler(
        service = service,
        store = store,
        node = node,
        conferenceAlias = conferenceAlias,
        participantId = response.participantId
    )

    override fun leave() {
        callHandler.dispose()
        tokenHandler.dispose()
    }
}
