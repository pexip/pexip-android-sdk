package com.pexip.sdk.media

/**
 * [MediaConnection] configuration.
 *
 * @property signaling an instance of [MediaConnectionSignaling] to be used by [MediaConnection]
 * @property iceServers a list of [IceServer]s that [MediaConnection] will use
 * @property dscp true if DSCP is enabled, false otherwise
 * @property presentationInMain true if presentation will be mixed with main video feed, false otherwise
 */
public class MediaConnectionConfig private constructor(
    public val signaling: MediaConnectionSignaling,
    public val iceServers: List<IceServer>,
    public val dscp: Boolean,
    public val presentationInMain: Boolean,
) {

    /**
     * A builder for [MediaConnectionConfig].
     *
     * @property signaling an instance of [MediaConnectionSignaling]
     */
    public class Builder(private val signaling: MediaConnectionSignaling) {

        private val iceServers = ArrayList(signaling.iceServers)
        private var dscp = false
        private var presentationInMain = false

        /**
         * Adds an [IceServer] to this builder.
         *
         * @param iceServer an ICE server, either TURN or STUN
         * @return this builder
         */
        public fun addIceServer(iceServer: IceServer): Builder = apply {
            this.iceServers += iceServer
        }

        /**
         * Sets whether DSCP is enabled (default is false).
         *
         * DSCP (Differentiated Services Code Point) values mark individual packets and may be
         * beneficial in a variety of networks to improve QoS.
         *
         * See [RFC 8837](https://datatracker.ietf.org/doc/html/rfc8837) for more info.
         *
         * @param dscp true if DSCP is enabled, false otherwise
         * @return this builder
         */
        public fun dscp(dscp: Boolean): Builder = apply {
            this.dscp = dscp
        }

        /**
         * Sets whether presentation will be mixed with main video feed (default is false).
         *
         * @param presentationInMain true if presentation will be mixed with main video feed, false otherwise
         * @return this builder
         */
        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.presentationInMain = presentationInMain
        }

        /**
         * Builds [MediaConnectionConfig].
         *
         * @return an instance of [MediaConnectionConfig]
         */
        public fun build(): MediaConnectionConfig = MediaConnectionConfig(
            signaling = signaling,
            iceServers = iceServers,
            dscp = dscp,
            presentationInMain = presentationInMain
        )
    }
}
