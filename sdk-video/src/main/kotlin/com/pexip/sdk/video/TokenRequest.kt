package com.pexip.sdk.video

import okhttp3.HttpUrl

/**
 * Encapsulates a request for a [Token].
 */
public class TokenRequest private constructor(
    private val nodeAddress: HttpUrl,
    internal val alias: String,
    internal val displayName: String,
    internal val pin: String?,
    internal var idp: IdentityProvider?,
    internal var ssoToken: String?,
) {

    internal val url: HttpUrl = nodeAddress.newBuilder()
        .addPathSegments("api/client/v2/conferences")
        .addPathSegment(alias)
        .addPathSegment("request_token")
        .build()

    public class Builder {

        private var nodeAddress: HttpUrl? = null
        private var alias: String? = null
        private var displayName: String? = null
        private var pin: String? = null
        private var idp: IdentityProvider? = null
        private var ssoToken: String? = null

        /**
         * Sets the node address.
         *
         * @param nodeAddress a node address
         * @return this [Builder]
         */
        public fun nodeAddress(nodeAddress: HttpUrl): Builder = apply {
            this.nodeAddress = nodeAddress
        }

        /**
         * Sets the conference alias.
         *
         * @param alias a conference alias
         * @return this [Builder]
         */
        public fun alias(alias: String): Builder = apply {
            require(alias.isNotBlank()) { "alias is blank." }
            this.alias = alias.trim()
        }

        /**
         * Sets the display name.
         *
         * @param displayName a display name
         * @return this [Builder]
         */
        public fun displayName(displayName: String): Builder = apply {
            require(displayName.isNotBlank()) { "displayName is blank." }
            this.displayName = displayName.trim()
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
            nodeAddress = checkNotNull(nodeAddress) { "nodeAddress is not set." },
            alias = checkNotNull(alias) { "alias is not set." },
            displayName = checkNotNull(displayName) { "displayName is not set." },
            pin = pin,
            idp = idp,
            ssoToken = ssoToken
        )
    }
}
