package com.pexip.sdk.sample

import android.os.Parcel
import com.pexip.sdk.api.infinity.RequestTokenResponse
import kotlinx.parcelize.Parceler
import java.util.UUID

object RequestTokenResponseParceler : Parceler<RequestTokenResponse> {

    override fun create(parcel: Parcel): RequestTokenResponse = RequestTokenResponse(
        token = parcel.readString()!!,
        expires = parcel.readLong(),
        participantId = UUID.fromString(parcel.readString()!!),
        participantName = parcel.readString()!!
    )

    override fun RequestTokenResponse.write(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
        parcel.writeLong(expires)
        parcel.writeString(participantId.toString())
        parcel.writeString(participantName)
    }
}
