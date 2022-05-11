package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.media.MediaConnectionSignaling
import kotlin.reflect.KProperty

internal class RealMediaConnectionSignaling(
    private val store: TokenStore,
    private val participantStep: InfinityService.ParticipantStep,
) : MediaConnectionSignaling {

    var callStep: InfinityService.CallStep? by ThreadLocal()
    var pwds: Map<String, String>? by ThreadLocal()

    override fun onOffer(
        callType: String,
        description: String,
        presentationInMix: Boolean,
    ): String {
        val token = store.get()
        pwds = getUfragPwd(description)
        return when (val step = callStep) {
            null -> {
                val request = CallsRequest(
                    callType = callType,
                    sdp = description,
                    present = if (presentationInMix) "main" else null
                )
                val response = participantStep.calls(request, token).execute()
                callStep = participantStep.call(response.callId)
                callStep!!.ack(token).execute()
                response.sdp
            }
            else -> {
                val request = UpdateRequest(description)
                val response = step.update(request, token).execute()
                response.sdp
            }
        }
    }

    override fun onCandidate(candidate: String, mid: String) {
        val callStep = checkNotNull(callStep) { "callStep is not set." }
        val pwds = checkNotNull(pwds) { "pwds are not set." }
        val token = store.get()
        val ufrag = getUfrag(candidate)
        val request = NewCandidateRequest(
            candidate = candidate,
            mid = mid,
            ufrag = ufrag,
            pwd = pwds[ufrag]
        )
        callStep.newCandidate(request, token).execute()
    }

    override fun onAudioMuted() {
        participantStep.mute(store.get()).execute()
    }

    override fun onAudioUnmuted() {
        participantStep.unmute(store.get()).execute()
    }

    override fun onVideoMuted() {
        participantStep.videoMuted(store.get()).execute()
    }

    override fun onVideoUnmuted() {
        participantStep.videoUnmuted(store.get()).execute()
    }

    private fun getUfrag(candidate: String) =
        checkNotNull(CANDIDATE_UFRAG.matchEntire(candidate)?.groupValues?.get(1))

    private fun getUfragPwd(description: String) = buildMap {
        val iterator = description.splitToSequence("\r\n").iterator()
        while (iterator.hasNext()) {
            var line = iterator.next()
            val ufrag = SDP_UFRAG.matchEntire(line)?.groupValues?.get(1) ?: continue
            line = iterator.next()
            val pwd = SDP_PWD.matchEntire(line)?.groupValues?.get(1) ?: continue
            put(ufrag, pwd)
        }
    }

    companion object {

        val SDP_UFRAG = Regex("^a=ice-ufrag:(.+)$")
        val SDP_PWD = Regex("^a=ice-pwd:(.+)$")
        val CANDIDATE_UFRAG = Regex(".*\\bufrag\\s+(.+?)\\s+.*")
    }
}

private operator fun <T> ThreadLocal<T>.getValue(thisRef: Any?, property: KProperty<*>) = get()

private operator fun <T> ThreadLocal<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) =
    set(value)
