package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.conference.CallHandler
import com.pexip.sdk.video.conference.Conference
import com.pexip.sdk.video.token.Token
import okhttp3.OkHttpClient

internal class RealConference(client: OkHttpClient, token: Token) : Conference {

    private val store = TokenStore(token.token, token.expires)
    private val service: InfinityService = RealInfinityService(
        client = client,
        store = store,
        address = token.address,
        participantId = token.participantId,
    )
    private val tokenHandler = TokenHandler(store, service)

    override val callHandler: CallHandler = CallHandler(service)

    override fun leave() {
        callHandler.dispose()
        tokenHandler.dispose()
    }
}
