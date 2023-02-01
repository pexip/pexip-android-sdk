/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
