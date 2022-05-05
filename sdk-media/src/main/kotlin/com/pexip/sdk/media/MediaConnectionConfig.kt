package com.pexip.sdk.media

public class MediaConnectionConfig private constructor(
    public val signaling: MediaConnectionSignaling,
    public val iceServers: List<IceServer>,
    public val presentationInMain: Boolean,
) {

    public class Builder(private val signaling: MediaConnectionSignaling) {

        private val iceServers = mutableListOf<IceServer>()
        private var presentationInMain = false

        public fun addIceServer(iceServer: IceServer): Builder = apply {
            this.iceServers += iceServer
        }

        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.presentationInMain = presentationInMain
        }

        @Deprecated("Use LocalVideoTrack.startCapture() to control capture quality.")
        public fun mainQualityProfile(mainQualityProfile: QualityProfile): Builder = this

        public fun build(): MediaConnectionConfig = MediaConnectionConfig(
            signaling = signaling,
            iceServers = iceServers,
            presentationInMain = presentationInMain
        )
    }
}
