/*
 * Copyright 2022-2023 Pexip AS
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
 * [MediaConnection] configuration.
 *
 * @property signaling an instance of [MediaConnectionSignaling] to be used by [MediaConnection]
 * @property iceServers a list of [IceServer]s that [MediaConnection] will use
 * @property dscp true if DSCP is enabled, false otherwise
 * @property continualGathering true if ICE candidates will be gathered continually, false otherwise
 * @property presentationInMain true if presentation will be mixed with main video feed, false otherwise; ignored for direct media calls
 * @property farEndCameraControl true if far end camera control is supported, false otherwise
 */
public class MediaConnectionConfig private constructor(
    public val signaling: MediaConnectionSignaling,
    public val iceServers: List<IceServer>,
    public val dscp: Boolean,
    public val continualGathering: Boolean,
    public val presentationInMain: Boolean,
    public val farEndCameraControl: Boolean,
) {

    /**
     * A builder for [MediaConnectionConfig].
     *
     * @property signaling an instance of [MediaConnectionSignaling]
     */
    public class Builder(private val signaling: MediaConnectionSignaling) {

        private val iceServers = ArrayList(signaling.iceServers)
        private var dscp = false
        private var continualGathering = true
        private var presentationInMain = false
        private var farEndCameraControl = false

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
         * Sets whether continual gathering of ICE candidates is enabled (default is true).
         *
         * Continual gathering never completes and generally performs better when roaming between
         * different networks, but may cause connection failures when no ICE candidates were found.
         *
         * @param continualGathering true if ICE candidates will be gathered continually, false otherwise
         * @return this builder
         */
        public fun continualGathering(continualGathering: Boolean): Builder = apply {
            this.continualGathering = continualGathering
        }

        /**
         * Sets whether presentation will be mixed with main video feed (default is false).
         *
         * This value is ignored for direct media calls.
         *
         * @param presentationInMain true if presentation will be mixed with main video feed, false otherwise
         * @return this builder
         */
        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.presentationInMain = presentationInMain
        }

        /**
         * Sets whether far end camera control will be enabled.
         *
         * @param farEndCameraControl true if far end camera control is supported, false otherwise
         * @return this builder
         */
        public fun farEndCameraControl(farEndCameraControl: Boolean): Builder = apply {
            this.farEndCameraControl = farEndCameraControl
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
            continualGathering = continualGathering,
            presentationInMain = presentationInMain && !signaling.directMedia,
            farEndCameraControl = farEndCameraControl,
        )
    }
}
