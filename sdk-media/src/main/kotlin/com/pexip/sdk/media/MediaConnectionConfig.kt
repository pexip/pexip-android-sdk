package com.pexip.sdk.media

public class MediaConnectionConfig private constructor(
    public val signaling: MediaConnectionSignaling,
    public val iceServers: List<IceServer>,
    public val presentationInMain: Boolean,
    public val mainQualityProfile: QualityProfile,
) {

    public class Builder(private val signaling: MediaConnectionSignaling) {

        private val iceServers = mutableListOf<IceServer>()
        private var presentationInMain = false
        private var mainQualityProfile = QualityProfile.Medium

        public fun addIceServer(iceServer: IceServer): Builder = apply {
            this.iceServers += iceServer
        }

        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.presentationInMain = presentationInMain
        }

        public fun mainQualityProfile(mainQualityProfile: QualityProfile): Builder = apply {
            this.mainQualityProfile = mainQualityProfile
        }

        public fun build(): MediaConnectionConfig = MediaConnectionConfig(
            signaling = signaling,
            iceServers = iceServers,
            presentationInMain = presentationInMain,
            mainQualityProfile = mainQualityProfile
        )
    }
}
