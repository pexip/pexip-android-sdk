package com.pexip.sdk.media

public class IceServer private constructor(
    public val urls: Collection<String>,
    public val username: String,
    public val password: String,
) {

    public class Builder(private val urls: Collection<String>) {

        public constructor(url: String) : this(listOf(url))

        private var username: String = ""
        private var password: String = ""

        init {
            require(urls.isNotEmpty()) { "urls are empty." }
        }

        public fun username(username: String): Builder = apply {
            this.username = username
        }

        public fun password(password: String): Builder = apply {
            this.password = password
        }

        public fun build(): IceServer = IceServer(urls, username, password)
    }
}
