package com.pexip.sdk.video.token

import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.token.internal.RealTokenRequester
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.Future

public interface TokenRequester {

    /**
     * A callback that will be invoked after call to [request].
     */
    public interface Callback {

        /**
         * Invoked when token request completed without issues.
         *
         * @param requester a [TokenRequester] used to perform this operation
         * @param token an instance of [Token]
         */
        public fun onSuccess(requester: TokenRequester, token: Token)

        /**
         * Invoked when node resolution encountered an error.
         *
         * Errors may be one of the following:
         *  - [SsoRedirectException] if SSO flow should continue via the provided URL
         *  - [RequiredSsoException] if SSO authentication is required to proceed
         *  - [RequiredPinException] if either host or guest PIN is required (if PIN was null)
         *  - [InvalidPinException] if the supplied PIN is invalid
         *  - [NoSuchNodeException] if supplied node address doesn't have a deployment
         *  - [NoSuchConferenceException] if alias did not match any aliases or call routing rules
         *  - [IOException] if a network error was encountered during operation
         *
         * @param requester a [TokenRequester] used to perform this operation
         * @param t an error
         */
        public fun onFailure(requester: TokenRequester, t: Throwable)
    }

    /**
     * Requests a new token from the conferencing node.
     *
     * @param request a [TokenRequest] spec
     * @param callback a completion handler
     * @return a [Future] that may be used to cancel the operation
     */
    public fun request(request: TokenRequest, callback: Callback): Future<*>

    public companion object {

        @JvmStatic
        public fun create(client: OkHttpClient = OkHttpClient): TokenRequester =
            RealTokenRequester(client)
    }
}
