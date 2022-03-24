package com.pexip.sdk.video.api

import android.os.Parcelable
import com.pexip.sdk.video.api.internal.DurationParceler
import com.pexip.sdk.video.api.internal.DurationSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@Parcelize
@TypeParceler<Duration, DurationParceler>
public data class RequestTokenResponse(
    public val token: String,
    @Serializable(with = DurationSerializer::class)
    public val expires: Duration,
    @SerialName("participant_uuid")
    public val participantId: ParticipantId,
) : Parcelable
