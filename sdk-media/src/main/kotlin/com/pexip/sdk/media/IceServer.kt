package com.pexip.sdk.media

/**
 * An Interactive Connectivity Establishment (ICE) server and credentials to access it.
 *
 * An ICE server may have multiple URLs associated with it.
 *
 * @property urls a list of server URLs
 * @property username a username used to authenticate with the server
 * @property password a password used to authenticate with the server
 */
public class IceServer private constructor(
    public val urls: Collection<String>,
    public val username: String,
    public val password: String,
) {

    /**
     * A builder for [IceServer].
     *
     * @property urls a list of server URLs
     * @throws IllegalArgumentException if the list is empty
     */
    public class Builder(private val urls: Collection<String>) {

        /**
         * Creates a new [Builder] from a single server URL.
         *
         * @param url a server URL
         */
        public constructor(url: String) : this(listOf(url))

        private var username: String = ""
        private var password: String = ""

        init {
            require(urls.isNotEmpty()) { "urls are empty." }
        }

        /**
         * Sets the username.
         *
         * @param username a username used to authenticate with the server
         */
        public fun username(username: String): Builder = apply {
            this.username = username
        }

        /**
         * Sets the password.
         *
         * @param password a password used to authenticate with the server
         */
        public fun password(password: String): Builder = apply {
            this.password = password
        }

        /**
         * Returns an [IceServer].
         *
         * @return an ICE server
         */
        public fun build(): IceServer = IceServer(urls, username, password)
    }
}
