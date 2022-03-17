package com.pexip.sdk.video.token

import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.internal.HttpUrl
import com.pexip.sdk.video.node.Node
import okhttp3.HttpUrl

/**
 * Encapsulates a request for a [Token].
 */
public class TokenRequest private constructor(
    internal val node: Node,
    internal val joinDetails: JoinDetails,
    internal val pin: String?,
    internal val idp: IdentityProvider?,
    internal val ssoToken: String?,
) {

    internal val url: HttpUrl = HttpUrl(node.address) {
        addPathSegments("api/client/v2/conferences")
        addPathSegment(joinDetails.alias)
        addPathSegment("request_token")
    }

    public class Builder {

        private var node: Node? = null
        private var joinDetails: JoinDetails? = null
        private var pin: String? = null
        private var idp: IdentityProvider? = null
        private var ssoToken: String? = null

        /**
         * Sets the node.
         *
         * @param node a node
         * @return this [Builder]
         */
        public fun node(node: Node): Builder = apply {
            this.node = node
        }

        /**
         * Sets the conference alias.
         *
         * @param joinDetails a conference alias
         * @return this [Builder]
         */
        public fun joinDetails(joinDetails: JoinDetails): Builder = apply {
            this.joinDetails = joinDetails
        }

        /**
         * Sets the PIN for this request.
         *
         * @param pin a PIN
         * @return this [Builder]
         */
        public fun pin(pin: String): Builder = apply {
            this.pin = pin.trim()
        }

        /**
         * Sets the identity provider used to proceed with SSO flow.
         *
         * @param idp an identity provider
         * @return this [Builder]
         */
        public fun idp(idp: IdentityProvider): Builder = apply {
            this.idp = idp
        }

        /**
         * Sets the ssoToken received from the SSO flow.
         *
         * @param ssoToken an SSO token
         * @return this [Builder]
         */
        public fun ssoToken(ssoToken: String): Builder = apply {
            require(ssoToken.isNotBlank()) { "ssoToken is blank." }
            this.ssoToken = ssoToken.trim()
        }

        public fun build(): TokenRequest = TokenRequest(
            node = checkNotNull(node) { "node is not set." },
            joinDetails = checkNotNull(joinDetails) { "alias is not set." },
            pin = pin,
            idp = idp,
            ssoToken = ssoToken
        )
    }
}
