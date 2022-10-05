package com.pexip.sdk.sample

import android.os.Parcelable
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.net.URL

data class SampleState(
    val destination: SampleDestination,
    val cameraVideoTrack: CameraVideoTrack? = null,
    val microphoneAudioTrack: LocalAudioTrack? = null,
    val cameraCapturing: Boolean? = null,
    val microphoneCapturing: Boolean? = null,
)

sealed interface SampleDestination : Parcelable {

    @Parcelize
    object Permissions : SampleDestination

    @Parcelize
    object Preflight : SampleDestination

    @Parcelize
    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val response: @WriteWith<RequestTokenResponseParceler> RequestTokenResponse,
    ) : SampleDestination
}
