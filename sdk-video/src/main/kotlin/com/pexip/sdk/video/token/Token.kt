package com.pexip.sdk.video.token

import android.os.Parcelable
import com.pexip.sdk.video.internal.DurationParceler
import com.pexip.sdk.video.internal.HttpUrlParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okhttp3.HttpUrl
import kotlin.time.Duration

@Parcelize
@TypeParceler<HttpUrl, HttpUrlParceler>
@TypeParceler<Duration, DurationParceler>
public class Token internal constructor(
    internal val address: HttpUrl,
    internal val participantId: String,
    internal val token: String,
    internal val expires: Duration,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false
        if (address != other.address) return false
        if (participantId != other.participantId) return false
        if (token != other.token) return false
        if (expires != other.expires) return false
        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + participantId.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + expires.hashCode()
        return result
    }

    override fun toString(): String = "Token(address=$address, participantId=$participantId)"
}
